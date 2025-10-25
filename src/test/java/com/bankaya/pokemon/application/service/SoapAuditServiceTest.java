package com.bankaya.pokemon.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bankaya.pokemon.domain.model.SoapAuditLog;
import com.bankaya.pokemon.domain.ports.SoapAuditLogRepositoryPort;
import com.bankaya.pokemon.infrastructure.adapter.enums.RequestStatus;
import com.bankaya.pokemon.infrastructure.adapter.persistence.entity.SoapAuditLogEntity;

@ExtendWith(MockitoExtension.class)
class SoapAuditServiceTest {

    @Mock
    private SoapAuditLogRepositoryPort repository;

    @InjectMocks
    private SoapAuditService soapAuditService;

    private SoapAuditLog testAuditLog;

    @BeforeEach
    void setUp() {
        testAuditLog = SoapAuditLog.builder()
                .ipOrigin("192.168.1.100")
                .requestDate(LocalDateTime.now())
                .soapMethod("getPokemonName")
                .startTime(System.currentTimeMillis())
                .durationMs(150L)
                .requestPayload("<soap:Envelope><soap:Body><getPokemonName><name>pikachu</name></getPokemonName></soap:Body></soap:Envelope>")
                .responsePayload("<soap:Envelope><soap:Body><getPokemonNameResponse><name>pikachu</name></getPokemonNameResponse></soap:Body></soap:Envelope>")
                .status(RequestStatus.SUCCESS)
                .endpointClass("PokemonEndpoint")
                .build();
    }

    @Test
    void saveAuditLog_shouldSaveEntityWithFormattedXml() {
        doNothing().when(repository).save(any(SoapAuditLogEntity.class));

        soapAuditService.saveAuditLog(testAuditLog);

        verify(repository, timeout(2000)).save(any(SoapAuditLogEntity.class));

        ArgumentCaptor<SoapAuditLogEntity> captor = ArgumentCaptor.forClass(SoapAuditLogEntity.class);
        verify(repository).save(captor.capture());

        SoapAuditLogEntity savedEntity = captor.getValue();
        assertNotNull(savedEntity);
        assertEquals("192.168.1.100", savedEntity.getIpOrigin());
        assertEquals("getPokemonName", savedEntity.getSoapMethod());
        assertEquals(150L, savedEntity.getDurationMs());
        assertEquals(RequestStatus.SUCCESS, savedEntity.getStatus());
        assertEquals("PokemonEndpoint", savedEntity.getEndpointClass());

        assertFalse(savedEntity.getRequestPayload().contains("> <"));
        assertFalse(savedEntity.getResponsePayload().contains("> <"));
        assertTrue(savedEntity.getRequestPayload().contains("><"));
        assertTrue(savedEntity.getResponsePayload().contains("><"));
    }

    @Test
    void saveAuditLog_shouldHandleNullPayloads() {
        SoapAuditLog auditLogWithNulls = SoapAuditLog.builder()
                .ipOrigin("127.0.0.1")
                .soapMethod("testMethod")
                .durationMs(100L)
                .requestPayload(null)
                .responsePayload(null)
                .status(RequestStatus.SUCCESS)
                .build();

        doNothing().when(repository).save(any(SoapAuditLogEntity.class));

        soapAuditService.saveAuditLog(auditLogWithNulls);

        verify(repository, timeout(2000)).save(any(SoapAuditLogEntity.class));

        ArgumentCaptor<SoapAuditLogEntity> captor = ArgumentCaptor.forClass(SoapAuditLogEntity.class);
        verify(repository).save(captor.capture());

        SoapAuditLogEntity savedEntity = captor.getValue();
        assertNull(savedEntity.getRequestPayload());
        assertNull(savedEntity.getResponsePayload());
    }

    @Test
    void saveAuditLog_shouldSaveErrorStatus() {
        SoapAuditLog errorLog = testAuditLog
                .withStatus(RequestStatus.ERROR)
                .withErrorMessage("Connection timeout");

        doNothing().when(repository).save(any(SoapAuditLogEntity.class));

        soapAuditService.saveAuditLog(errorLog);

        verify(repository, timeout(2000)).save(any(SoapAuditLogEntity.class));

        ArgumentCaptor<SoapAuditLogEntity> captor = ArgumentCaptor.forClass(SoapAuditLogEntity.class);
        verify(repository).save(captor.capture());

        SoapAuditLogEntity savedEntity = captor.getValue();
        assertEquals(RequestStatus.ERROR, savedEntity.getStatus());
        assertEquals("Connection timeout", savedEntity.getErrorMessage());
    }

    @Test
    void saveAuditLog_shouldSaveFaultStatus() {
        SoapAuditLog faultLog = testAuditLog
                .withStatus(RequestStatus.FAULT)
                .withErrorMessage("SOAP Fault: Pokemon not found");

        doNothing().when(repository).save(any(SoapAuditLogEntity.class));

        soapAuditService.saveAuditLog(faultLog);

        verify(repository, timeout(2000)).save(any(SoapAuditLogEntity.class));

        ArgumentCaptor<SoapAuditLogEntity> captor = ArgumentCaptor.forClass(SoapAuditLogEntity.class);
        verify(repository).save(captor.capture());

        SoapAuditLogEntity savedEntity = captor.getValue();
        assertEquals(RequestStatus.FAULT, savedEntity.getStatus());
        assertEquals("SOAP Fault: Pokemon not found", savedEntity.getErrorMessage());
    }

    @Test
    void saveAuditLog_shouldHandleRepositoryException() {
        doThrow(new RuntimeException("Database connection error"))
                .when(repository).save(any(SoapAuditLogEntity.class));

        assertDoesNotThrow(() -> soapAuditService.saveAuditLog(testAuditLog));

        verify(repository, timeout(2000)).save(any(SoapAuditLogEntity.class));

        verify(repository).save(any(SoapAuditLogEntity.class));
    }

    @Test
    void saveAuditLog_shouldFormatXmlByRemovingWhitespace() {
        String xmlWithSpaces = "<root>  <child>  <grandchild>value</grandchild>  </child>  </root>";
        SoapAuditLog logWithSpaces = testAuditLog
                .withRequestPayload(xmlWithSpaces)
                .withResponsePayload(xmlWithSpaces);

        doNothing().when(repository).save(any(SoapAuditLogEntity.class));

        soapAuditService.saveAuditLog(logWithSpaces);

        verify(repository, timeout(2000)).save(any(SoapAuditLogEntity.class));

        ArgumentCaptor<SoapAuditLogEntity> captor = ArgumentCaptor.forClass(SoapAuditLogEntity.class);
        verify(repository).save(captor.capture());

        SoapAuditLogEntity savedEntity = captor.getValue();
        String expectedFormatted = "<root><child><grandchild>value</grandchild></child></root>";
        assertEquals(expectedFormatted, savedEntity.getRequestPayload());
        assertEquals(expectedFormatted, savedEntity.getResponsePayload());
    }

    @Test
    void saveAuditLog_shouldTrimWhitespaceInPayload() {
        String xmlWithLeadingTrailing = "   <root><child>value</child></root>   ";
        SoapAuditLog logWithWhitespace = testAuditLog
                .withRequestPayload(xmlWithLeadingTrailing);

        doNothing().when(repository).save(any(SoapAuditLogEntity.class));

        soapAuditService.saveAuditLog(logWithWhitespace);

        verify(repository, timeout(2000)).save(any(SoapAuditLogEntity.class));

        ArgumentCaptor<SoapAuditLogEntity> captor = ArgumentCaptor.forClass(SoapAuditLogEntity.class);
        verify(repository).save(captor.capture());

        SoapAuditLogEntity savedEntity = captor.getValue();
        assertEquals("<root><child>value</child></root>", savedEntity.getRequestPayload());
        assertFalse(savedEntity.getRequestPayload().startsWith(" "));
        assertFalse(savedEntity.getRequestPayload().endsWith(" "));
    }

    @Test
    void saveAuditLog_shouldHandleComplexXmlStructure() {
        SoapAuditLog logWithComplexXml = getSoapAuditLog();

        doNothing().when(repository).save(any(SoapAuditLogEntity.class));

        soapAuditService.saveAuditLog(logWithComplexXml);

        verify(repository, timeout(2000)).save(any(SoapAuditLogEntity.class));

        ArgumentCaptor<SoapAuditLogEntity> captor = ArgumentCaptor.forClass(SoapAuditLogEntity.class);
        verify(repository).save(captor.capture());

        SoapAuditLogEntity savedEntity = captor.getValue();
        assertNotNull(savedEntity.getResponsePayload());
        assertFalse(savedEntity.getResponsePayload().contains("> <"));
    }

    private SoapAuditLog getSoapAuditLog() {
        String complexXml = """
                <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                    <soap:Body>
                        <getPokemonAbilitiesResponse>
                            <abilities>
                                <ability>
                                    <name>overgrow</name>
                                    <url>https://pokeapi.co/api/v2/ability/65/</url>
                                </ability>
                            </abilities>
                        </getPokemonAbilitiesResponse>
                    </soap:Body>
                </soap:Envelope>
                """;

        return testAuditLog.withResponsePayload(complexXml);
    }

    @Test
    void saveAuditLog_shouldPreserveXmlContent() {
        String xmlContent = "<data>Important content with spaces inside</data>";
        SoapAuditLog log = testAuditLog
                .withRequestPayload(xmlContent);

        doNothing().when(repository).save(any(SoapAuditLogEntity.class));

        soapAuditService.saveAuditLog(log);

        verify(repository, timeout(2000)).save(any(SoapAuditLogEntity.class));

        ArgumentCaptor<SoapAuditLogEntity> captor = ArgumentCaptor.forClass(SoapAuditLogEntity.class);
        verify(repository).save(captor.capture());

        SoapAuditLogEntity savedEntity = captor.getValue();
        assertTrue(savedEntity.getRequestPayload().contains("Important content with spaces inside"));
    }

    @Test
    void saveAuditLog_shouldHandleEmptyStrings() {
        SoapAuditLog logWithEmptyStrings = testAuditLog
                .withRequestPayload("")
                .withResponsePayload("")
                .withErrorMessage("");

        doNothing().when(repository).save(any(SoapAuditLogEntity.class));

        soapAuditService.saveAuditLog(logWithEmptyStrings);

        verify(repository, timeout(2000)).save(any(SoapAuditLogEntity.class));

        ArgumentCaptor<SoapAuditLogEntity> captor = ArgumentCaptor.forClass(SoapAuditLogEntity.class);
        verify(repository).save(captor.capture());

        SoapAuditLogEntity savedEntity = captor.getValue();
        assertEquals("", savedEntity.getRequestPayload());
        assertEquals("", savedEntity.getResponsePayload());
        assertEquals("", savedEntity.getErrorMessage());
    }

    @Test
    void saveAuditLog_shouldMapAllFieldsCorrectly() {
        doNothing().when(repository).save(any(SoapAuditLogEntity.class));

        soapAuditService.saveAuditLog(testAuditLog);

        verify(repository, timeout(2000)).save(any(SoapAuditLogEntity.class));

        ArgumentCaptor<SoapAuditLogEntity> captor = ArgumentCaptor.forClass(SoapAuditLogEntity.class);
        verify(repository).save(captor.capture());

        SoapAuditLogEntity savedEntity = captor.getValue();

        assertEquals(testAuditLog.getIpOrigin(), savedEntity.getIpOrigin());
        assertEquals(testAuditLog.getSoapMethod(), savedEntity.getSoapMethod());
        assertEquals(testAuditLog.getDurationMs(), savedEntity.getDurationMs());
        assertEquals(testAuditLog.getStatus(), savedEntity.getStatus());
        assertEquals(testAuditLog.getEndpointClass(), savedEntity.getEndpointClass());
        assertNotNull(savedEntity.getRequestPayload());
        assertNotNull(savedEntity.getResponsePayload());
    }

}
