package com.klp.notification.messaging.domain.repository;

import com.klp.notification.messaging.domain.entity.SlackMessage;

public interface SlackMessageRepository {

    SlackMessage save(SlackMessage slackMessage);
}
