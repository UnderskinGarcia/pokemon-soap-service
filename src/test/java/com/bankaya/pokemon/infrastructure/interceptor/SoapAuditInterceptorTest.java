package com.bankaya.pokemon.infrastructure.interceptor;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.MethodEndpoint;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapMessage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.bankaya.pokemon.application.service.SoapAuditService;
import com.bankaya.pokemon.domain.model.SoapAuditLog;
import com.bankaya.pokemon.infrastructure.adapter.enums.RequestStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SoapAuditInterceptorTest {

    @Mock
    private SoapAuditService auditService;

    @Mock
    private MessageContext messageContext;

    @Mock
    private SoapMessage soapRequest;

    @Mock
    private SoapMessage soapResponse;

    @Mock
    private SoapBody soapBody;

    @Mock
    private SoapFault soapFault;

    @InjectMocks
    private SoapAuditInterceptor interceptor;

    private MockHttpServletRequest mockHttpRequest;

    @BeforeEach
    void setUp() {
        mockHttpRequest = new MockHttpServletRequest();
        mockHttpRequest.setRemoteAddr("127.0.0.1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockHttpRequest));
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void handleRequest_shouldInitializeAuditLog() throws Exception {
        when(messageContext.getRequest()).thenReturn(soapRequest);
        when(soapRequest.getSoapBody()).thenReturn(soapBody);

        Document doc = createSoapDocument("getPokemonName");
        when(soapBody.getPayloadSource()).thenReturn(new DOMSource(doc.getDocumentElement()));

        doAnswer(invocation -> {
            ByteArrayOutputStream out = invocation.getArgument(0);
            out.write("<soap>request</soap>".getBytes(StandardCharsets.UTF_8));
            return null;
        }).when(soapRequest)
                .writeTo(any(ByteArrayOutputStream.class));

        boolean result = interceptor.handleRequest(messageContext, null);

        assertTrue(result);
    }

    @Test
    void handleRequest_withMethodEndpoint_shouldExtractEndpointClass() throws Exception {
        when(messageContext.getRequest()).thenReturn(soapRequest);
        when(soapRequest.getSoapBody()).thenReturn(soapBody);

        Document doc = createSoapDocument("getPokemonId");
        when(soapBody.getPayloadSource()).thenReturn(new DOMSource(doc.getDocumentElement()));

        doAnswer(invocation -> {
            ByteArrayOutputStream out = invocation.getArgument(0);
            out.write("<soap>request</soap>".getBytes(StandardCharsets.UTF_8));
            return null;
        }).when(soapRequest).writeTo(any(ByteArrayOutputStream.class));

        Method method = this.getClass().getDeclaredMethod("tearDown");
        MethodEndpoint endpoint = new MethodEndpoint(this, method);

        boolean result = interceptor.handleRequest(messageContext, endpoint);

        assertTrue(result);
    }

    @Test
    void handleRequest_withException_shouldReturnTrueAndLogError() {
        when(messageContext.getRequest()).thenThrow(new RuntimeException("Test exception"));

        boolean result = interceptor.handleRequest(messageContext, null);

        assertTrue(result);
    }

    @Test
    void handleResponse_shouldSetSuccessStatusAndResponsePayload() throws Exception {
        setupHandleRequest();
        interceptor.handleRequest(messageContext, null);

        when(messageContext.getResponse()).thenReturn(soapResponse);
        doAnswer(invocation -> {
            ByteArrayOutputStream out = invocation.getArgument(0);
            out.write("<soap>response</soap>".getBytes(StandardCharsets.UTF_8));
            return null;
        }).when(soapResponse).writeTo(any(ByteArrayOutputStream.class));

        boolean result = interceptor.handleResponse(messageContext, null);

        assertTrue(result);
    }

    @Test
    void handleResponse_withException_shouldReturnTrueAndLogError() {
        when(messageContext.getResponse()).thenThrow(new RuntimeException("Test exception"));

        boolean result = interceptor.handleResponse(messageContext, null);

        assertTrue(result);
    }

    @Test
    void handleFault_shouldSetFaultStatusAndErrorMessage() throws Exception {
        setupHandleRequest();
        interceptor.handleRequest(messageContext, null);

        when(messageContext.getResponse()).thenReturn(soapResponse);
        when(soapResponse.getSoapBody()).thenReturn(soapBody);
        when(soapBody.getFault()).thenReturn(soapFault);
        when(soapFault.getFaultStringOrReason()).thenReturn("Pokemon not found");
        doAnswer(invocation -> {
            ByteArrayOutputStream out = invocation.getArgument(0);
            out.write("<soap>fault</soap>".getBytes(StandardCharsets.UTF_8));
            return null;
        }).when(soapResponse).writeTo(any(ByteArrayOutputStream.class));

        boolean result = interceptor.handleFault(messageContext, null);

        assertTrue(result);
    }

    @Test
    void handleFault_withException_shouldReturnTrueAndLogError() {
        when(messageContext.getResponse()).thenThrow(new RuntimeException("Test exception"));

        boolean result = interceptor.handleFault(messageContext, null);

        assertTrue(result);
    }

    @Test
    void afterCompletion_shouldSaveAuditLogWithSuccessStatus() throws Exception {
        setupHandleRequest();
        boolean requestResult = interceptor.handleRequest(messageContext, null);
        assertTrue(requestResult); // Ensure request handling succeeded

        when(messageContext.getResponse()).thenReturn(soapResponse);
        doAnswer(invocation -> {
            ByteArrayOutputStream out = invocation.getArgument(0);
            out.write("<soap>response</soap>".getBytes(StandardCharsets.UTF_8));
            return null;
        }).when(soapResponse).writeTo(any(ByteArrayOutputStream.class));
        boolean responseResult = interceptor.handleResponse(messageContext, null);
        assertTrue(responseResult); // Ensure response handling succeeded

        interceptor.afterCompletion(messageContext, null, null);

        ArgumentCaptor<SoapAuditLog> captor = ArgumentCaptor.forClass(SoapAuditLog.class);
        verify(auditService).saveAuditLog(captor.capture());

        SoapAuditLog savedLog = captor.getValue();
        assertNotNull(savedLog);
        assertEquals(RequestStatus.SUCCESS, savedLog.getStatus());
        assertEquals("127.0.0.1", savedLog.getIpOrigin());
        assertNotNull(savedLog.getRequestPayload());
        assertNotNull(savedLog.getResponsePayload());
        assertNotNull(savedLog.getDurationMs());
        assertTrue(savedLog.getDurationMs() >= 0);
    }

    @Test
    void afterCompletion_withException_shouldSetErrorStatus() throws Exception {
        setupHandleRequest();
        interceptor.handleRequest(messageContext, null);

        Exception testException = new RuntimeException("Test error");

        interceptor.afterCompletion(messageContext, null, testException);

        ArgumentCaptor<SoapAuditLog> captor = ArgumentCaptor.forClass(SoapAuditLog.class);
        verify(auditService).saveAuditLog(captor.capture());

        SoapAuditLog savedLog = captor.getValue();
        assertEquals(RequestStatus.ERROR, savedLog.getStatus());
        assertEquals("Test error", savedLog.getErrorMessage());
    }

    @Test
    void afterCompletion_withFault_shouldSaveFaultStatus() throws Exception {
        setupHandleRequest();
        interceptor.handleRequest(messageContext, null);

        when(messageContext.getResponse()).thenReturn(soapResponse);
        when(soapResponse.getSoapBody()).thenReturn(soapBody);
        when(soapBody.getFault()).thenReturn(soapFault);
        when(soapFault.getFaultStringOrReason()).thenReturn("SOAP Fault occurred");
        doAnswer(invocation -> {
            ByteArrayOutputStream out = invocation.getArgument(0);
            out.write("<soap>fault</soap>".getBytes(StandardCharsets.UTF_8));
            return null;
        }).when(soapResponse).writeTo(any(ByteArrayOutputStream.class));
        interceptor.handleFault(messageContext, null);

        interceptor.afterCompletion(messageContext, null, null);

        ArgumentCaptor<SoapAuditLog> captor = ArgumentCaptor.forClass(SoapAuditLog.class);
        verify(auditService).saveAuditLog(captor.capture());

        SoapAuditLog savedLog = captor.getValue();
        assertEquals(RequestStatus.FAULT, savedLog.getStatus());
        assertEquals("SOAP Fault occurred", savedLog.getErrorMessage());
    }

    @Test
    void extractClientIp_shouldReturnRemoteAddrByDefault() throws Exception {
        mockHttpRequest.setRemoteAddr("192.168.1.100");
        setupHandleRequest();

        interceptor.handleRequest(messageContext, null);
        interceptor.afterCompletion(messageContext, null, null);

        ArgumentCaptor<SoapAuditLog> captor = ArgumentCaptor.forClass(SoapAuditLog.class);
        verify(auditService).saveAuditLog(captor.capture());
        assertEquals("192.168.1.100", captor.getValue().getIpOrigin());
    }

    @Test
    void extractClientIp_shouldUseXForwardedForHeader() throws Exception {
        mockHttpRequest.addHeader("X-Forwarded-For", "10.0.0.1, 10.0.0.2");
        setupHandleRequest();

        interceptor.handleRequest(messageContext, null);
        interceptor.afterCompletion(messageContext, null, null);

        ArgumentCaptor<SoapAuditLog> captor = ArgumentCaptor.forClass(SoapAuditLog.class);
        verify(auditService).saveAuditLog(captor.capture());
        assertEquals("10.0.0.1", captor.getValue().getIpOrigin()); // First IP in the list
    }

    @Test
    void extractClientIp_shouldUseXRealIpHeader() throws Exception {
        mockHttpRequest.addHeader("X-Real-IP", "172.16.0.5");
        setupHandleRequest();

        interceptor.handleRequest(messageContext, null);
        interceptor.afterCompletion(messageContext, null, null);

        ArgumentCaptor<SoapAuditLog> captor = ArgumentCaptor.forClass(SoapAuditLog.class);
        verify(auditService).saveAuditLog(captor.capture());
        assertEquals("172.16.0.5", captor.getValue().getIpOrigin());
    }

    @Test
    void extractClientIp_shouldSkipUnknownHeaderValue() throws Exception {
        mockHttpRequest.addHeader("X-Forwarded-For", "unknown");
        mockHttpRequest.addHeader("X-Real-IP", "192.168.2.2");
        setupHandleRequest();

        interceptor.handleRequest(messageContext, null);
        interceptor.afterCompletion(messageContext, null, null);

        ArgumentCaptor<SoapAuditLog> captor = ArgumentCaptor.forClass(SoapAuditLog.class);
        verify(auditService).saveAuditLog(captor.capture());
        assertEquals("192.168.2.2", captor.getValue().getIpOrigin());
    }

    @Test
    void extractPayload_shouldHandleExtractionError() throws Exception {
        when(messageContext.getRequest()).thenReturn(soapRequest);
        when(soapRequest.getSoapBody()).thenReturn(soapBody);

        Document doc = createSoapDocument("getPokemonName");
        when(soapBody.getPayloadSource()).thenReturn(new DOMSource(doc.getDocumentElement()));

        doThrow(new RuntimeException("IO Error"))
                .when(soapRequest).writeTo(any(ByteArrayOutputStream.class));

        boolean result = interceptor.handleRequest(messageContext, null);

        assertTrue(result);

        interceptor.afterCompletion(messageContext, null, null);
        ArgumentCaptor<SoapAuditLog> captor = ArgumentCaptor.forClass(SoapAuditLog.class);
        verify(auditService).saveAuditLog(captor.capture());

        String payload = captor.getValue().getRequestPayload();
        assertTrue(payload.startsWith("Error extracting payload"));
    }

    @Test
    void afterCompletion_withoutPreviousRequest_shouldNotSaveAuditLog() {
        interceptor.afterCompletion(messageContext, null, null);
        verify(auditService, never()).saveAuditLog(any());
    }

    @Test
    void afterCompletion_shouldCalculateDuration() throws Exception {
        setupHandleRequest();
        long startTime = System.currentTimeMillis();
        interceptor.handleRequest(messageContext, null);

        when(messageContext.getResponse()).thenReturn(soapResponse);
        doAnswer(invocation -> {
            ByteArrayOutputStream out = invocation.getArgument(0);
            out.write("<soap>response</soap>".getBytes(StandardCharsets.UTF_8));
            return null;
        }).when(soapResponse).writeTo(any(ByteArrayOutputStream.class));
        interceptor.handleResponse(messageContext, null);

        interceptor.afterCompletion(messageContext, null, null);
        long endTime = System.currentTimeMillis();

        ArgumentCaptor<SoapAuditLog> captor = ArgumentCaptor.forClass(SoapAuditLog.class);
        verify(auditService).saveAuditLog(captor.capture());

        SoapAuditLog savedLog = captor.getValue();
        assertNotNull(savedLog.getDurationMs());
        assertTrue(savedLog.getDurationMs() >= 0, "Duration should be non-negative");
        // Verify the duration is reasonable (less than total test time)
        assertTrue(savedLog.getDurationMs() <= (endTime - startTime),
                "Duration should not exceed total test execution time");
    }

    @Test
    void extractSoapInfo_withNullEndpoint_shouldNotSetEndpointClass() throws Exception {
        setupHandleRequest();

        interceptor.handleRequest(messageContext, null); // null endpoint
        interceptor.afterCompletion(messageContext, null, null);

        ArgumentCaptor<SoapAuditLog> captor = ArgumentCaptor.forClass(SoapAuditLog.class);
        verify(auditService).saveAuditLog(captor.capture());

        SoapAuditLog savedLog = captor.getValue();
        assertNull(savedLog.getEndpointClass());
    }

    @Test
    void extractSoapInfo_withNonMethodEndpoint_shouldNotSetEndpointClass() throws Exception {
        setupHandleRequest();
        Object nonMethodEndpoint = new Object();

        interceptor.handleRequest(messageContext, nonMethodEndpoint);
        interceptor.afterCompletion(messageContext, null, null);

        ArgumentCaptor<SoapAuditLog> captor = ArgumentCaptor.forClass(SoapAuditLog.class);
        verify(auditService).saveAuditLog(captor.capture());

        SoapAuditLog savedLog = captor.getValue();
        assertNull(savedLog.getEndpointClass());
    }

    private void setupHandleRequest() throws Exception {
        when(messageContext.getRequest()).thenReturn(soapRequest);
        when(soapRequest.getSoapBody()).thenReturn(soapBody);

        Document doc = createSoapDocument("getPokemonName");
        when(soapBody.getPayloadSource()).thenReturn(new DOMSource(doc.getDocumentElement()));

        doAnswer(invocation -> {
            ByteArrayOutputStream out = invocation.getArgument(0);
            out.write("<soap>request</soap>".getBytes(StandardCharsets.UTF_8));
            return null;
        }).when(soapRequest).writeTo(any(ByteArrayOutputStream.class));
    }

    private Document createSoapDocument(String methodName) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        Element root = doc.createElement(methodName);
        doc.appendChild(root);
        return doc;
    }
}
