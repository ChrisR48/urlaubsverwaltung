package org.synyx.urlaubsverwaltung.sicknote.sicknote;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
class SickNoteMailConfiguration implements SchedulingConfigurer {

    private final SickNoteProperties sickNoteProperties;
    private final SickNoteMailService sickNoteMailService;

    @Autowired
    SickNoteMailConfiguration(SickNoteProperties sickNoteProperties, SickNoteMailService sickNoteMailService) {
        this.sickNoteProperties = sickNoteProperties;
        this.sickNoteMailService = sickNoteMailService;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        scheduledTaskRegistrar.addCronTask(sickNoteMailService::sendEndOfSickPayNotification, sickNoteProperties.getEndOfPayNotification().getCron());
    }
}
