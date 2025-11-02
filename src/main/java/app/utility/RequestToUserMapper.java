package app.utility;

import app.user.model.User;
import app.web.dto.ProfileEditRequest;
import lombok.experimental.UtilityClass;

@UtilityClass
public class RequestToUserMapper {
        public static ProfileEditRequest fromUserToEditRequest(User user) {

        return ProfileEditRequest.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .profilePicture(user.getProfilePicture())
                .build();
    }
}
