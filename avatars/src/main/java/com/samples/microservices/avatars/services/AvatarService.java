package com.samples.microservices.avatars.services;

import com.samples.microservices.avatars.exceptions.AvatarNotFound;
import com.samples.microservices.avatars.model.Avatar;
import com.timgroup.jgravatar.Gravatar;
import com.timgroup.jgravatar.GravatarRating;
import org.springframework.stereotype.Service;

@Service
public class AvatarService {
    public Avatar getAvatar(String user, String[] emails) throws AvatarNotFound {
        for(String email : emails) {
            Gravatar gravatar = new Gravatar();
            gravatar.setSize(50);
            gravatar.setRating(GravatarRating.GENERAL_AUDIENCES);
            String[] parts = email.split("_");
            String fullemail = user + "@" + parts[0] + "." + parts[1];
            String url = gravatar.getUrl(fullemail);
            byte[] jpg = gravatar.download(fullemail);
            if(jpg != null) {
                return new Avatar(user, url);
            }
        }
        throw new AvatarNotFound(user);
    }
}
