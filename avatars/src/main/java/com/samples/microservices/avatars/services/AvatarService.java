package com.samples.microservices.avatars.services;

import com.timgroup.jgravatar.Gravatar;
import com.timgroup.jgravatar.GravatarRating;
import org.springframework.stereotype.Service;

@Service
public class AvatarService {
    public String getAvatar(String user, String[] emails) {
        for(String email : emails) {
            Gravatar gravatar = new Gravatar();
            gravatar.setSize(50);
            gravatar.setRating(GravatarRating.GENERAL_AUDIENCES);
            String[] parts = email.split("_");
            String fullemail = user + "@" + parts[0] + "." + parts[1];
            String url = gravatar.getUrl(fullemail);
            byte[] jpg = gravatar.download(fullemail);
            if(jpg != null) {
                return url;
            }
        }
        return null;
    }
}
