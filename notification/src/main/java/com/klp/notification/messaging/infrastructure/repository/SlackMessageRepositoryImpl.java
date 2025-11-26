package com.klp.notification.messaging.infrastructure.repository;

import com.klp.notification.messaging.domain.entity.SlackMessage;
import com.klp.notification.messaging.domain.repository.SlackMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SlackMessageRepositoryImpl implements SlackMessageRepository {

    private final SlackMessageJpaRepository slackMessageJpaRepository;

    @Override
    public SlackMessage save(SlackMessage slackMessage) {
        return slackMessageJpaRepository.save(slackMessage);
    }
}
