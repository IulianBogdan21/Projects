package socialNetwork.utilitaries.events;

import socialNetwork.domain.models.FriendRequest;
import socialNetwork.domain.models.Friendship;

public class FriendRequestChangeEvent implements Event<FriendRequest> {

    private ChangeEventType type;
    private FriendRequest data, oldData;

    public FriendRequestChangeEvent(ChangeEventType type, FriendRequest data) {
        this.type = type;
        this.data = data;
    }
    public FriendRequestChangeEvent(ChangeEventType type, FriendRequest data, FriendRequest oldData) {
        this.type = type;
        this.data = data;
        this.oldData=oldData;
    }

    @Override
    public ChangeEventType getType() {
        return type;
    }

    @Override
    public FriendRequest getData() {
        return data;
    }

    @Override
    public FriendRequest getOldData() {
        return oldData;
    }
}
