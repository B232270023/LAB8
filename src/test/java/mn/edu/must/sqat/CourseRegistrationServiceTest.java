package mn.edu.must.sqat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CourseRegistrationServiceTest {
    @Mock StudentRepository repository;
    @Mock EmailSender emailSender;
    CourseRegistrationService service;

    @BeforeEach
    void setUp() {
        service = new CourseRegistrationService(repository, emailSender);
    }

    private Student activeStudent() {
        return new Student("S001", "student@example.edu", true, List.of("CS101"));
    }

    private void stubSuccessfulPath() {
        when(repository.findStudent("S001")).thenReturn(activeStudent());
        when(repository.courseExists("CS201")).thenReturn(true);
        when(repository.prerequisites("CS201")).thenReturn(List.of("CS101"));
    }

    @Test
    void activeStudent_registersSuccessfully_savesAndSendsEmail() {
        stubSuccessfulPath();

        assertEquals("OK", service.register("S001", "CS201"));

        verify(repository).saveRegistration("S001", "CS201");
        verify(emailSender).send(eq("student@example.edu"), eq("Бүртгэл баталгаажлаа"), contains("CS201"));
    }

    @Test
    void missingStudent_registersRejected_doesNotSaveOrSendEmail() {
        when(repository.findStudent("S404")).thenReturn(null);

        assertEquals("ERROR_NO_STUDENT", service.register("S404", "CS201"));

        verify(repository, never()).saveRegistration(anyString(), anyString());
        verifyNoInteractions(emailSender);
    }

    @Test
    void inactiveStudent_registersRejected_doesNotInteractWithEmailSender() {
        when(repository.findStudent("S001")).thenReturn(new Student("S001", "student@example.edu", false, List.of()));

        assertEquals("ERROR_INACTIVE_STUDENT", service.register("S001", "CS201"));

        verify(repository, never()).saveRegistration(anyString(), anyString());
        verifyNoInteractions(emailSender);
    }

    @Test
    void missingPrerequisite_registersRejected_doesNotSave() {
        when(repository.findStudent("S001")).thenReturn(activeStudent());
        when(repository.courseExists("CS201")).thenReturn(true);
        when(repository.prerequisites("CS201")).thenReturn(List.of("CS101", "CS102"));

        assertEquals("ERROR_PREREQUISITES", service.register("S001", "CS201"));

        verify(repository, never()).saveRegistration(anyString(), anyString());
        verifyNoInteractions(emailSender);
    }

    @Test
    void emailSenderThrows_registrationStillSucceedsAndIsSaved() {
        stubSuccessfulPath();
        doThrow(new RuntimeException("SMTP unavailable"))
                .when(emailSender).send(anyString(), anyString(), anyString());

        assertEquals("OK", service.register("S001", "CS201"));

        verify(repository).saveRegistration("S001", "CS201");
        verify(emailSender).send(anyString(), anyString(), anyString());
    }

    @Test
    void nullInput_registerThrowsWithoutInteractions() {
        assertThrows(IllegalArgumentException.class, () -> service.register(null, "CS201"));
        assertThrows(IllegalArgumentException.class, () -> service.register("S001", null));
        verifyNoInteractions(repository, emailSender);
    }

    @Test
    void successfulRegistration_emailBodyContainsCourseCode() {
        stubSuccessfulPath();
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

        assertEquals("OK", service.register("S001", "CS201"));

        verify(emailSender).send(eq("student@example.edu"), eq("Бүртгэл баталгаажлаа"), bodyCaptor.capture());
        assertTrue(bodyCaptor.getValue().contains("CS201"));
    }

    @ParameterizedTest(name = "{0} returns {1}")
    @CsvSource({
        "NO_STUDENT, ERROR_NO_STUDENT",
        "NO_COURSE, ERROR_NO_COURSE",
        "PREREQUISITES, ERROR_PREREQUISITES"
    })
    void registrationError_scenarioReturnsExpectedResult(String scenario, String expected) {
        lenient().when(repository.findStudent("S001")).thenReturn(scenario.equals("NO_STUDENT") ? null : activeStudent());
        lenient().when(repository.courseExists("CS201")).thenReturn(!scenario.equals("NO_COURSE"));
        lenient().when(repository.prerequisites("CS201"))
                .thenReturn(scenario.equals("PREREQUISITES") ? List.of("CS999") : List.of("CS101"));

        assertEquals(expected, service.register("S001", "CS201"));
        verify(repository, never()).saveRegistration(anyString(), anyString());
        verifyNoInteractions(emailSender);
    }
}
