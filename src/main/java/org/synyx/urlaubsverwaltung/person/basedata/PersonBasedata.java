package org.synyx.urlaubsverwaltung.person.basedata;

public class PersonBasedata {

    private final int personId;
    private final String personnelNumber;
    private final String additionalInformation;

    public PersonBasedata(int personId, String personnelNumber, String additionalInformation) {
        this.personId = personId;
        this.personnelNumber = personnelNumber;
        this.additionalInformation = additionalInformation;
    }

    public int getPersonId() {
        return personId;
    }

    public String getPersonnelNumber() {
        return personnelNumber;
    }

    public String getAdditionalInformation() {
        return additionalInformation;
    }
}
