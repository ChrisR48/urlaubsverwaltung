package org.synyx.urlaubsverwaltung.sicknote.comment;

import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.sicknote.sicknote.SickNote;

import java.util.List;

/**
 * Service for handling {@link SickNoteCommentEntity}s.
 */
public interface SickNoteCommentService {

    /**
     * Creates a comment for the given sick note with the given action. The given person defines the author of the
     * comment.
     *
     * @param sickNote to create the comment for
     * @param action   describes the lifecycle action of the sick note that will be saved in the comment
     * @param author   of the comment
     * @return the created comment
     */
    SickNoteCommentEntity create(SickNote sickNote, SickNoteCommentAction action, Person author);

    /**
     * Creates a comment for the given sick note with the given action. The given person defines the author of the
     * comment.
     *
     * @param sickNote to create the comment for
     * @param action   describes the lifecycle action of the sick note that will be saved in the comment
     * @param author   of the comment
     * @param text     of the comment
     * @return the created comment
     */
    SickNoteCommentEntity create(SickNote sickNote, SickNoteCommentAction action, Person author, String text);

    /**
     * Gets all comments for the given sick note.
     *
     * @param sickNote to get the comments for
     * @return all comments for the given sick note.
     */
    List<SickNoteCommentEntity> getCommentsBySickNote(SickNote sickNote);
}
