package com.nixsolutions.ponarin.web_service.soap;

import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import javax.xml.ws.soap.SOAPFaultException;

import com.nixsolutions.ponarin.web_service.soap.impl.SoapUserServiceImplService;

public class SoapUserServiceTest {
    private static final long NON_EXISTS_ID = -1L;
    private static final String NON_EXISTS_LOGIN = "nonExistsLogin";
    private static final String NON_EXISTS_EMAIL = "non_exists_email@email.ua";

    private static SoapUserService soapUserService;

    private User testUser;

    @BeforeClass
    public static void generalSetUp() throws Exception {
        soapUserService = new SoapUserServiceImplService()
                .getSoapUserServiceImplPort();
    }

    @Before
    public void setUp() {
        testUser = getDefaultUser();
        soapUserService.create(testUser);
        User actualUser = soapUserService.findByLogin(testUser.getLogin());

        if (actualUser == null) {
            throw new RuntimeException("User can't be null");
        }

        testUser.setId(actualUser.getId());
    }

    @After
    public void tearDown() {
        User user = soapUserService.findById(testUser.getId());
        if (user != null) {
            soapUserService.remove(testUser);
        }
    }

    @Test
    public void testCreate() {
        tearDown();
        testUser = getDefaultUser();

        soapUserService.create(testUser);

        User actualUser = soapUserService.findByLogin(testUser.getLogin());

        assertNotNull("User must exists in db", actualUser);
        testUser.setId(actualUser.getId());
        equalsUsers(testUser, actualUser);
    }

    @Test(expected = SOAPFaultException.class)
    public void testCreateLoginDublicate() {
        testUser.setEmail(NON_EXISTS_EMAIL);
        soapUserService.create(testUser);
    }

    @Test(expected = SOAPFaultException.class)
    public void testCreateWithEmailDublicate() {
        testUser.setLogin(NON_EXISTS_LOGIN);
        soapUserService.create(testUser);
    }

    @Test
    public void testUpdate() {
        testUser.setPassword(testUser.getPassword() + "12erph6");
        testUser.setEmail(NON_EXISTS_EMAIL);
        testUser.setFirstName("updated first name");
        testUser.setLastName("updated last name");
        testUser.setBirthDay(getXmlCalendar(10, 5, 2000));

        Role role = new Role();
        role.setId(2);
        role.setName("User");
        testUser.setRole(role);

        soapUserService.update(testUser);

        User actualUser = soapUserService.findById(testUser.getId());

        equalsUsers(testUser, actualUser);
    }

    @Test(expected = SOAPFaultException.class)
    public void testUpdateNonExistsId() {
        User user = getDefaultUser();
        user.setId(NON_EXISTS_ID);
        soapUserService.update(user);
    }

    @Test(expected = SOAPFaultException.class)
    public void testUpdateEmailDublicate() {
        List<User> users = soapUserService.findAll();
        testUser.setLogin("newTestLogin");
        testUser.setEmail(users.get(0).getEmail());
        soapUserService.update(testUser);
    }

    @Test
    public void testRemove() {
        soapUserService.remove(testUser);
        User dbUser = soapUserService.findById(testUser.getId());
        assertNull("Deleted user still in db", dbUser);
    }

    @Test
    public void testRemoveWithNonExistsUser() {
        User user = getDefaultUser();
        user.setId(NON_EXISTS_ID);
        user.setLogin(NON_EXISTS_LOGIN);
        user.setEmail(NON_EXISTS_EMAIL);

        try {
            soapUserService.remove(user);
            fail("Exception during removing non exists user");
        } catch (Exception ex) {
        }
    }

    @Test
    public void testFindAll() {
        List<User> userList = soapUserService.findAll();

        assertTrue("User list size must be grater or equals 2",
                userList.size() >= 2);
    }

    @Test
    public void testFindById() {
        User actualUser = soapUserService.findById(testUser.getId());
        equalsUsers(testUser, actualUser);
    }

    @Test
    public void testFindByIdWithNonExistsId() {
        User user = soapUserService.findById(-1L);
        assertNull("User must be null", user);
    }

    @Test
    public void testFindByLogin() {
        User user = soapUserService.findByLogin(testUser.getLogin());
        assertNotNull("User must exists in db", user);
        equalsUsers(testUser, user);
    }

    @Test
    public void testFindByLoginWithNonExistsLogin() {
        User user = soapUserService.findByLogin("nonExistsLogin");
        assertNull("User must be null", user);
    }

    @Test
    public void testFindByEmail() {
        User user = soapUserService.findByEmail(testUser.getEmail());
        assertNotNull("User must exists in db", user);
        equalsUsers(testUser, user);
    }

    private User getDefaultUser() {
        User user = new User();

        user.setLogin("testLogin");
        user.setPassword("Q!q1q1");
        user.setEmail("testEmail@mail.ru");
        user.setFirstName("First Name");
        user.setLastName("Last Name");
        user.setBirthDay(getXmlCalendar(7, 10, 1990));

        Role role = new Role();
        role.setId(1);
        role.setName("Admin");
        user.setRole(role);

        return user;
    }

    private XMLGregorianCalendar getXmlCalendar(int day, int month, int year) {
        GregorianCalendar calendar = new GregorianCalendar(year, month, day);
        try {
            return DatatypeFactory.newInstance()
                    .newXMLGregorianCalendar(calendar);
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void equalsUsers(User expected, User actual) {
        assertEquals("Id not equals", expected.getId(), actual.getId());
        assertEquals("Login not equals", expected.getLogin(),
                actual.getLogin());
        assertEquals("Password not equals", expected.getPassword(),
                actual.getPassword());
        assertEquals("Email not equals", expected.getEmail(),
                actual.getEmail());
        assertEquals("First name not equals", expected.getFirstName(),
                actual.getFirstName());
        assertEquals("Last name not equals", expected.getLastName(),
                actual.getLastName());
        assertEquals("Birthday not equals", expected.getBirthDay(),
                actual.getBirthDay());
        assertEquals("Role id not equals", expected.getRole().getId(),
                actual.getRole().getId());
        assertEquals("Role name not equals", expected.getRole().getName(),
                actual.getRole().getName());
    }
}
