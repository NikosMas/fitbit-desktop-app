package com.fitbit.grad.services.builders;

import com.fitbit.grad.models.CollectionEnum;
import com.fitbit.grad.models.CommonDataSample;
import com.fitbit.grad.models.HeartRateCategoryEnum;
import com.fitbit.grad.models.HeartRateValue;
import com.fitbit.grad.services.authRequests.AuthCodeRequestService;
import com.fitbit.grad.services.notification.HeartRateFilterService;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.*;
import com.vaadin.ui.Notification.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static com.vaadin.ui.Notification.show;

/**
 * Service about Vaadin buttons building
 *
 * @author nikos_mas, alex_kak
 */

@Service
public class ButtonsBuilderService {


    private final static Logger LOG = LoggerFactory.getLogger("Fitbit application");
    private static String OS = System.getProperty("os.name");
    private final MongoTemplate mongoTemplate;
    private final Environment env;
    private final HeartRateFilterService heartRateFilterService;
    private final AuthCodeRequestService codeService;
    private final RedisTemplate<String, String> redisTemplate;

    @Autowired
    public ButtonsBuilderService(MongoTemplate mongoTemplate, Environment env,
                                 HeartRateFilterService heartRateFilterService, AuthCodeRequestService codeService,
                                 RedisTemplate<String, String> redisTemplate) {
        this.heartRateFilterService = heartRateFilterService;
        this.mongoTemplate = mongoTemplate;
        this.redisTemplate = redisTemplate;
        this.codeService = codeService;
        this.env = env;
    }

    public void authorizationBuilder(Button authorizationCode, TextField clientId, TextField clientSecret, Button exit) {
        authorizationCode.setIcon(VaadinIcons.CHECK_CIRCLE);
        authorizationCode.setCaption("Submit");
        authorizationCode.setWidth("150");
        authorizationCode.addClickListener(click -> {
            if (!clientId.isEmpty() && !clientSecret.isEmpty()) {
                redisTemplate.opsForValue().set("Client-id", clientId.getValue());
                redisTemplate.opsForValue().set("Client-secret", clientSecret.getValue());
                authorizationCode.setEnabled(false);
                clientId.setEnabled(false);
                clientSecret.setEnabled(false);
                codeService.codeRequest();
//                collectionsService.collectionsCreate();
            } else {
                show("Complete with valid client id and client secret given from to your account at Fitbit", Type.ERROR_MESSAGE);
            }
        });
    }

    public void heartRateMailBuilder(Button skip, Button heartRateMail, TextField mail, TextField heartRate,
                                     ComboBox<HeartRateCategoryEnum> select, VerticalLayout content) {
        heartRateMail.setIcon(VaadinIcons.CHECK_CIRCLE);
        heartRateMail.setCaption("Submit");
        heartRateMail.setWidth("150");
        heartRateMail.addClickListener(click -> {
            if (!mail.getValue().isEmpty() && !heartRate.getValue().isEmpty() && mail.getValue().contains("@")
                    && !select.isEmpty()) {
                try {
                    heartRateFilterService.heartRateSelect(mail.getValue(), Long.valueOf(heartRate.getValue()),
                            select.getValue(), content);
                    heartRateMail.setEnabled(false);
                    select.setEnabled(false);
                    mail.setEnabled(false);
                    heartRate.setEnabled(false);
                    skip.setEnabled(false);
                    LOG.info("Mail successfully sent to user with heart rate information");
                    show("Mail successfully sent to user with heart rate information!");
                } catch (NumberFormatException e) {
                    show("Complete the minutes field with number", Type.ERROR_MESSAGE);
                }
            } else {
                show(
                        "Complete the required fields with a valid e-mail & number of minutes and choose category",
                        Type.ERROR_MESSAGE);
            }
        });
    }

    public boolean continueBuilder(Button submitCheckBoxButton, CheckBoxGroup<String> multiCheckBox) {

        if (submitCheckBoxButton.isEnabled()) {
            show("Complete the required steps before", Type.ERROR_MESSAGE);
//            return false;
        } else if (!multiCheckBox.getValue().contains("HeartRate data")) {
            return false;
        }
        return true;
    }

    public void downloadBuilder(Button download) {
        download.setIcon(VaadinIcons.DOWNLOAD);
        download.setCaption("Download");
        download.setWidth("150");
        download.addClickListener(click -> {
            if (mongoTemplate.collectionExists(CollectionEnum.A_STEPS.d())) {
                if (mongoTemplate.findAll(CommonDataSample.class, CollectionEnum.A_STEPS.d()).isEmpty()
                        && mongoTemplate.findAll(CommonDataSample.class, CollectionEnum.S_MINUTES_AWAKE.d()).isEmpty()
                        && mongoTemplate.findAll(HeartRateValue.class, CollectionEnum.FILTERD_A_HEART.d()).isEmpty())
                    show("No user data available for downloading", Type.ERROR_MESSAGE);
                else {
                    try {
                        if (OS.equalsIgnoreCase("linux")) {
                            Runtime.getRuntime().exec("xdg-open " + env.getProperty("downloadProps.exportUrl"));
                        } else {
                            Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + env.getProperty("downloadProps.exportUrl"));
                        }
                    } catch (IOException e) {
                        LOG.error("Something went wrong: ", e);
                    }
                }
            } else {
                show("No user data available for downloading", Type.ERROR_MESSAGE);
            }
        });
    }

//    public void platformBuilder(Button platform) {
//        platform.setIcon(VaadinIcons.ARROW_FORWARD);
//        platform.setCaption("Go To Platform");
//        platform.setWidth("160");
//        platform.addClickListener(click -> {
//            if (mongoTemplate.findAll(CommonDataSample.class, CollectionEnum.A_STEPS.d()).isEmpty()
//                    && mongoTemplate.findAll(CommonDataSample.class, CollectionEnum.S_MINUTES_AWAKE.d()).isEmpty()
//                    && mongoTemplate.findAll(HeartRateValue.class, CollectionEnum.FILTERD_A_HEART.d()).isEmpty()) {
//                show("No user data available for downloading", Type.ERROR_MESSAGE);
//            } else {
//                try {
//                    if (OS.equalsIgnoreCase("linux")) {
//                        Runtime.getRuntime().exec("xdg-open " + platformProperties.getGoToPlatformUrl());
//                    } else {
//                        Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + platformProperties.getGoToPlatformUrl());
//                    }
//                } catch (IOException e) {
//                    LOG.error("Something went wrong: ", e);
//                }
//            }
//        });
//    }
}
