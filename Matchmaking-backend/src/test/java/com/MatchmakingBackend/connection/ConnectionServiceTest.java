package com.MatchmakingBackend.connection;

import com.MatchmakingBackend.profile.Profile;
import com.MatchmakingBackend.profile.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ConnectionServiceTest {

    private ProfileRepository profileRepository;
    private InterestRequestRepository interestRequestRepository;
    private ConversationRepository conversationRepository;
    private ChatMessageRepository chatMessageRepository;
    private ChatMessageBroadcaster chatMessageBroadcaster;
    private ConnectionService connectionService;

    @BeforeEach
    void setUp() {
        profileRepository = mock(ProfileRepository.class);
        interestRequestRepository = mock(InterestRequestRepository.class);
        conversationRepository = mock(ConversationRepository.class);
        chatMessageRepository = mock(ChatMessageRepository.class);
        chatMessageBroadcaster = mock(ChatMessageBroadcaster.class);

        connectionService = new ConnectionService(
                profileRepository, interestRequestRepository, conversationRepository,
                chatMessageRepository, chatMessageBroadcaster);
    }

    @Test
    void sendRequest_rejectsSelfRequest() {
        CreateInterestRequest command = new CreateInterestRequest(1L, 1L);

        assertThatThrownBy(() -> connectionService.sendRequest(command))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("cannot send an interest request to yourself");

        verifyNoInteractions(interestRequestRepository);
    }

    @Test
    void sendRequest_returnsExistingRequestInsteadOfDuplicating() {
        Profile sender = profileWithId(1L);
        Profile receiver = profileWithId(2L);
        InterestRequest existing = new InterestRequest();
        existing.setSenderProfile(sender);
        existing.setReceiverProfile(receiver);

        when(profileRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(profileRepository.findById(2L)).thenReturn(Optional.of(receiver));
        when(interestRequestRepository.findBetweenProfiles(1L, 2L)).thenReturn(List.of(existing));

        connectionService.sendRequest(new CreateInterestRequest(1L, 2L));

        verify(interestRequestRepository, never()).save(any());
    }

    @Test
    void acceptRequest_rejectsWhenCallerIsNotReceiver() {
        InterestRequest request = new InterestRequest();
        request.setReceiverProfile(profileWithId(2L));

        when(interestRequestRepository.findById(10L)).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> connectionService.acceptRequest(10L, 999L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Only the receiver can accept");
    }

    @Test
    void getMessages_rejectsNonParticipant() {
        Conversation conversation = new Conversation();
        conversation.setProfileOne(profileWithId(1L));
        conversation.setProfileTwo(profileWithId(2L));

        when(conversationRepository.findById(5L)).thenReturn(Optional.of(conversation));

        assertThatThrownBy(() -> connectionService.getMessages(5L, 999L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("not part of this conversation");
    }

    @Test
    void sendMessage_broadcastsToParticipants() {
        Profile sender = profileWithId(1L);
        Conversation conversation = new Conversation();
        conversation.setProfileOne(sender);
        conversation.setProfileTwo(profileWithId(2L));

        when(conversationRepository.findById(5L)).thenReturn(Optional.of(conversation));
        when(profileRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        connectionService.sendMessage(5L, new SendChatMessageRequest(1L, "Hello!"));

        verify(chatMessageBroadcaster).broadcast(any());
    }

    private static Profile profileWithId(Long id) {
        Profile profile = new Profile();
        profile.setId(id);
        profile.setFullName("Profile " + id);
        profile.setGender("MALE");
        return profile;
    }
}