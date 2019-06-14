package org.synyx.urlaubsverwaltung.security.ldap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.synyx.urlaubsverwaltung.person.Person;
import org.synyx.urlaubsverwaltung.person.PersonService;
import org.synyx.urlaubsverwaltung.person.Role;
import org.synyx.urlaubsverwaltung.security.PersonSyncService;
import org.synyx.urlaubsverwaltung.testdatacreator.TestDataCreator;

import javax.naming.Name;
import javax.naming.NamingException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static java.util.Optional.empty;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Unit test for {@link LdapPersonContextMapper}.
 */
public class LdapPersonContextMapperTest {

    private LdapPersonContextMapper ldapPersonContextMapper;

    private PersonService personService;
    private PersonSyncService personSyncService;
    private LdapUserMapper ldapUserMapper;

    private DirContextOperations context;

    @Before
    public void setUp() {

        personService = mock(PersonService.class);
        personSyncService = mock(PersonSyncService.class);
        ldapUserMapper = mock(LdapUserMapper.class);

        ldapPersonContextMapper = new LdapPersonContextMapper(personService, personSyncService, ldapUserMapper);

        context = mock(DirContextOperations.class);

        when(context.getDn()).thenReturn(mock(Name.class));
        when(context.getStringAttributes("cn")).thenReturn(new String[]{"First", "Last"});
        when(context.getStringAttribute(anyString())).thenReturn("Foo");
    }


    @Test(expected = IllegalArgumentException.class)
    public void ensureThrowsIfTryingToGetAuthoritiesForNullPerson() {

        ldapPersonContextMapper.getGrantedAuthorities(null);
    }


    @Test(expected = IllegalStateException.class)
    public void ensureThrowsIfTryingToGetAuthoritiesOfPersonWithNoRoles() {

        Person person = TestDataCreator.createPerson();
        person.setPermissions(Collections.emptyList());

        ldapPersonContextMapper.getGrantedAuthorities(person);
    }


    @Test
    public void ensureReturnsCorrectListOfAuthoritiesUsingTheRolesOfTheGivenPerson() {

        Person person = TestDataCreator.createPerson();
        person.setPermissions(Arrays.asList(Role.USER, Role.BOSS));

        Collection<GrantedAuthority> authorities = ldapPersonContextMapper.getGrantedAuthorities(person);

        Assert.assertEquals("Wrong number of authorities", 2, authorities.size());
        Assert.assertTrue("No authority for user role found",
            SecurityTestUtil.authorityForRoleExists(authorities, Role.USER));
        Assert.assertTrue("No authority for boss role found",
            SecurityTestUtil.authorityForRoleExists(authorities, Role.BOSS));
    }


    @Test
    public void ensureCreatesPersonIfPersonDoesNotExist() throws NamingException,
        UnsupportedMemberAffiliationException {

        when(ldapUserMapper.mapFromContext(eq(context)))
            .thenReturn(new LdapUser("murygina", Optional.of("Aljona"), Optional.of("Murygina"),
                Optional.of("murygina@synyx.de")));
        when(personService.getPersonByLogin(anyString())).thenReturn(empty());
        when(personSyncService.createPerson(anyString(), any(), any(), any())).thenReturn(TestDataCreator.createPerson());

        ldapPersonContextMapper.mapUserFromContext(context, "murygina", null);

        verify(ldapUserMapper).mapFromContext(context);
        verify(personSyncService)
            .createPerson(eq("murygina"), eq(Optional.of("Aljona")),
                eq(Optional.of("Murygina")), eq(Optional.of("murygina@synyx.de")));
    }


    @Test
    public void ensureSyncsPersonDataUsingLDAPAttributes() throws NamingException,
        UnsupportedMemberAffiliationException {

        Person person = TestDataCreator.createPerson();
        person.setPermissions(Collections.singletonList(Role.USER));

        when(ldapUserMapper.mapFromContext(eq(context)))
            .thenReturn(new LdapUser("murygina", Optional.of("Aljona"), Optional.of("Murygina"),
                Optional.of("murygina@synyx.de")));
        when(personService.getPersonByLogin(anyString())).thenReturn(Optional.of(person));
        when(personSyncService.syncPerson(any(Person.class), any(), any(), any())).thenReturn(person);

        ldapPersonContextMapper.mapUserFromContext(context, "murygina", null);

        verify(ldapUserMapper).mapFromContext(context);
        verify(personSyncService)
            .syncPerson(eq(person), eq(Optional.of("Aljona")), eq(Optional.of("Murygina")),
                eq(Optional.of("murygina@synyx.de")));
    }


    @Test
    public void ensureUsernameIsBasedOnLdapUsername() throws NamingException, UnsupportedMemberAffiliationException {

        String userIdentifier = "mgroehning";
        String userNameSignedInWith = "mgroehning@simpsons.com";

        when(ldapUserMapper.mapFromContext(eq(context)))
            .thenReturn(new LdapUser("mgroehning", empty(), empty(), empty()));
        when(personService.getPersonByLogin(anyString())).thenReturn(empty());
        when(personSyncService.createPerson(anyString(), any(), any(), any())).thenReturn(TestDataCreator.createPerson());

        UserDetails userDetails = ldapPersonContextMapper.mapUserFromContext(context, userNameSignedInWith, null);

        Assert.assertNotNull("Username should be set", userDetails.getUsername());
        Assert.assertEquals("Wrong username", userIdentifier, userDetails.getUsername());
    }


    @Test(expected = DisabledException.class)
    public void ensureLoginIsNotPossibleIfUserIsDeactivated() throws UnsupportedMemberAffiliationException,
        NamingException {

        Person person = TestDataCreator.createPerson();
        person.setPermissions(Collections.singletonList(Role.INACTIVE));

        when(personService.getPersonByLogin(anyString())).thenReturn(Optional.of(person));
        when(ldapUserMapper.mapFromContext(eq(context)))
            .thenReturn(new LdapUser(person.getLoginName(), Optional.of(person.getFirstName()),
                Optional.of(person.getLastName()), Optional.of(person.getEmail())));
        when(personSyncService.syncPerson(any(Person.class), any(), any(), any())).thenReturn(person);

        ldapPersonContextMapper.mapUserFromContext(context, person.getLoginName(), null);
    }


    @Test(expected = BadCredentialsException.class)
    public void ensureLoginIsNotPossibleIfLdapUserCanNotBeCreatedBecauseOfInvalidUserIdentifier()
        throws NamingException, UnsupportedMemberAffiliationException {

        when(ldapUserMapper.mapFromContext(eq(context)))
            .thenThrow(new InvalidSecurityConfigurationException("Bad!"));

        ldapPersonContextMapper.mapUserFromContext(context, "username", null);
    }


    @Test(expected = BadCredentialsException.class)
    public void ensureLoginIsNotPossibleIfLdapUserHasNotSupportedMemberOfAttribute() throws NamingException,
        UnsupportedMemberAffiliationException {

        when(ldapUserMapper.mapFromContext(eq(context)))
            .thenThrow(new UnsupportedMemberAffiliationException("Bad!"));

        ldapPersonContextMapper.mapUserFromContext(context, "username", null);
    }


    @Test
    public void ensureAuthoritiesAreBasedOnRolesOfTheSignedInPerson() throws NamingException,
        UnsupportedMemberAffiliationException {

        Person person = TestDataCreator.createPerson("username");
        person.setPermissions(Arrays.asList(Role.USER, Role.BOSS));

        when(ldapUserMapper.mapFromContext(eq(context)))
            .thenReturn(new LdapUser("username", empty(), empty(), empty()));
        when(personService.getPersonByLogin(anyString())).thenReturn(Optional.of(person));
        when(personSyncService.syncPerson(any(Person.class), any(), any(), any())).thenReturn(person);

        UserDetails userDetails = ldapPersonContextMapper.mapUserFromContext(context, "username", null);

        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        Assert.assertEquals("Wrong number of authorities", 2, authorities.size());
        Assert.assertTrue("Missing authority for user role",
            SecurityTestUtil.authorityForRoleExists(authorities, Role.USER));
        Assert.assertTrue("Missing authority for boss role",
            SecurityTestUtil.authorityForRoleExists(authorities, Role.BOSS));
    }


    @Test
    public void ensureAddsOfficeRoleToSignedInUserIfNoUserWithOfficeRoleExistsYet() throws NamingException,
        UnsupportedMemberAffiliationException {

        Person person = TestDataCreator.createPerson("username");
        person.setPermissions(Collections.singletonList(Role.USER));

        when(ldapUserMapper.mapFromContext(eq(context)))
            .thenReturn(new LdapUser("username", empty(), empty(), empty()));

        when(personService.getPersonByLogin(anyString())).thenReturn(Optional.of(person));
        when(personService.getPersonsByRole(Role.OFFICE)).thenReturn(Collections.emptyList());
        when(personSyncService.syncPerson(any(Person.class), any(), any(), any())).thenReturn(person);

        ldapPersonContextMapper.mapUserFromContext(context, "username", null);

        verify(personService).getPersonsByRole(Role.OFFICE);
        verify(personSyncService).appointPersonAsOfficeUser(person);
    }
}
