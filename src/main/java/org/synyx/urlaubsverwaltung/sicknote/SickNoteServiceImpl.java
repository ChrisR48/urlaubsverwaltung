package org.synyx.urlaubsverwaltung.sicknote;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.settings.SickNoteSettingsEntity;
import org.synyx.urlaubsverwaltung.sicknote.settings.SickNoteSettingsService;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementation for {@link org.synyx.urlaubsverwaltung.sicknote.SickNoteService}.
 */
@Service
class SickNoteServiceImpl implements SickNoteService {

    private final SickNoteRepository sickNoteRepository;
    private final SickNoteSettingsService sickNoteSettingsService;
    private final Clock clock;

    @Autowired
    public SickNoteServiceImpl(SickNoteRepository sickNoteRepository, SickNoteSettingsService sickNoteSettingsService, Clock clock) {
        this.sickNoteRepository = sickNoteRepository;
        this.sickNoteSettingsService = sickNoteSettingsService;
        this.clock = clock;
    }

    @Override
    public void save(SickNote sickNote) {
        sickNoteRepository.save(sickNote);
    }

    @Override
    public Optional<SickNote> getById(Integer id) {
        return sickNoteRepository.findById(id);
    }

    @Override
    public List<SickNote> getByPersonAndPeriod(Person person, LocalDate from, LocalDate to) {
        return sickNoteRepository.findByPersonAndPeriod(person, from, to);
    }

    @Override
    public List<SickNote> getByPeriod(LocalDate from, LocalDate to) {
        return sickNoteRepository.findByPeriod(from, to);
    }

    @Override
    public List<SickNote> getSickNotesReachingEndOfSickPay() {

        final SickNoteSettingsEntity sickNoteSettings = sickNoteSettingsService.getSettings();

        final LocalDate endOfSickPayDate = ZonedDateTime.now(clock)
            .plusDays(sickNoteSettings.getDaysBeforeEndOfSickPayNotification())
            .toLocalDate();

        return sickNoteRepository.findSickNotesToNotifyForSickPayEnd(sickNoteSettings.getMaximumSickPayDays(), endOfSickPayDate);
    }

    @Override
    public List<SickNote> getAllActiveByYear(int year) {
        return sickNoteRepository.findAllActiveByYear(year);
    }

    @Override
    public Long getNumberOfPersonsWithMinimumOneSickNote(int year) {
        return sickNoteRepository.findNumberOfPersonsWithMinimumOneSickNote(year);
    }

    @Override
    public List<SickNote> getForStates(List<SickNoteStatus> sickNoteStatuses) {
        return sickNoteRepository.findByStatusIn(sickNoteStatuses);
    }

    @Override
    public List<SickNote> getForStatesAndPersonSince(List<SickNoteStatus> sickNoteStatuses, List<Person> persons, LocalDate since) {
        return sickNoteRepository.findByStatusInAndPersonInAndEndDateIsGreaterThanEqual(sickNoteStatuses, persons, since);
    }

    @Override
    public List<SickNote> getForStatesAndPerson(List<SickNoteStatus> sickNoteStatus, List<Person> persons, LocalDate start, LocalDate end) {
        return sickNoteRepository.findByStatusInAndPersonInAndEndDateIsGreaterThanEqualAndStartDateIsLessThanEqual(sickNoteStatus, persons, start, end);
    }

    @Override
    public void setEndOfSickPayNotificationSend(SickNote sickNote) {

        sickNote.setEndOfSickPayNotificationSend(LocalDate.now(clock));
        sickNoteRepository.save(sickNote);
    }
}
