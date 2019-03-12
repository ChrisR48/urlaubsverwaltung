package org.synyx.urlaubsverwaltung.restapi.availability;

import org.joda.time.DateMidnight;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.synyx.urlaubsverwaltung.core.person.Person;
import org.synyx.urlaubsverwaltung.core.person.PersonService;
import org.synyx.urlaubsverwaltung.restapi.ApiExceptionHandlerControllerAdvice;
import org.synyx.urlaubsverwaltung.test.TestDataCreator;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * @author Timo Eifler - eifler@synyx.de
 */
public class AvailabilityControllerTest {

    private MockMvc mockMvc;

    private PersonService personServiceMock;
    private AvailabilityService availabilityServiceMock;
    private Person testPerson;
    private String loginName;

    @Before
    public void setUp() {

        loginName = "login";

        preparePersonServiceMock();

        availabilityServiceMock = mock(AvailabilityService.class);

        mockMvc = MockMvcBuilders.standaloneSetup(new AvailabilityController(availabilityServiceMock,
            personServiceMock)).setControllerAdvice(new ApiExceptionHandlerControllerAdvice()).build();
    }


    private void preparePersonServiceMock() {

        personServiceMock = mock(PersonService.class);

        testPerson = TestDataCreator.createPerson("testPerson");

        when(personServiceMock.getPersonByLogin(anyString())).thenReturn(Optional.of(testPerson));
    }


    @Test
    public void ensureFetchesAvailabilitiesForGivenPersonIfProvided() throws Exception {

        mockMvc.perform(get("/api/availabilities").param("from", "2016-01-01")
            .param("to", "2016-01-31")
            .param("person", loginName))
            .andExpect(status().isOk());

        verify(personServiceMock).getPersonByLogin(loginName);

        verify(availabilityServiceMock)
            .getPersonsAvailabilities(eq(new DateMidnight(2016, 1, 1)),
                eq(new DateMidnight(2016, 01, 31)), eq(testPerson));
    }


    @Test
    public void ensureRequestsAreOnlyAllowedForADateRangeOfMaxOneMonth() throws Exception {

        mockMvc.perform(get("/api/availabilities").param("from", "2016-01-01")
            .param("to", "2016-02-01")
            .param("person", loginName))
            .andExpect(status().isBadRequest());
    }


    @Test
    public void ensureBadRequestForMissingPersonParameter() throws Exception {

        mockMvc.perform(get("/api/availabilities").param("to", "2016-12-31").param("to", "2016-12-31"))
            .andExpect(status().isBadRequest());
    }


    @Test
    public void ensureBadRequestForMissingFromParameter() throws Exception {

        mockMvc.perform(get("/api/availabilities").param("to", "2016-12-31").param("person", loginName))
            .andExpect(status().isBadRequest());
    }


    @Test
    public void ensureBadRequestForInvalidFromParameter() throws Exception {

        mockMvc.perform(get("/api/availabilities").param("from", "foo")
            .param("to", "2016-12-31")
            .param("person", loginName))
            .andExpect(status().isBadRequest());
    }


    @Test
    public void ensureBadRequestForMissingToParameter() throws Exception {

        mockMvc.perform(get("/api/availabilities").param("from", "2016-01-01").param("person", loginName))
            .andExpect(status().isBadRequest());
    }


    @Test
    public void ensureBadRequestForInvalidToParameter() throws Exception {

        mockMvc.perform(get("/api/availabilities").param("from", "2016-01-01")
            .param("to", "foo")
            .param("person", loginName))
            .andExpect(status().isBadRequest());
    }


    @Test
    public void ensureBadRequestForInvalidPeriod() throws Exception {

        mockMvc.perform(get("/api/availabilities").param("from", "2016-01-01")
            .param("to", "2015-01-01")
            .param("person", loginName))
            .andExpect(status().isBadRequest());
    }
}
