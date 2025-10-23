package com.bankaya.pokemon.infrastructure.interceptor;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.endpoint.MethodEndpoint;
import org.springframework.ws.soap.SoapMessage;
import org.w3c.dom.Node;

import com.bankaya.pokemon.application.service.SoapAuditService;
import com.bankaya.pokemon.domain.model.SoapAuditLog;
import com.bankaya.pokemon.infrastructure.adapter.enums.RequestStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import jakarta.servlet.http.HttpServletRequest;

@Component
@Log4j2
@RequiredArgsConstructor
public class SoapAuditInterceptor implements EndpointInterceptor {

    private final SoapAuditService auditService;
    private static final ThreadLocal<SoapAuditLog> soapAuditLog = new ThreadLocal<>();

    @Override
    public boolean handleRequest(MessageContext messageContext, Object endpoint) throws Exception {
        try {
            // Iniciar contexto de auditoría
            SoapAuditLog auditLog = SoapAuditLog.builder().build()
                    .withIpOrigin(extractClientIp())
                    .withRequestDate(LocalDateTime.now())
                    .withStartTime(System.currentTimeMillis())
                    .withRequestPayload(extractPayload(messageContext.getRequest()));

            // Extraer información del SOAP
            extractSoapInfo(messageContext, auditLog, endpoint);

            // Guardar en ThreadLocal
            soapAuditLog.set(auditLog);

            log.debug("Starting SOAP request - Method: {}, IP: {}",
                    auditLog.getSoapMethod(), auditLog.getIpOrigin());

            return true;
        } catch (Exception e) {
            log.error("Error in handleRequest interceptor", e);
            return true;
        }
    }

    @Override
    public boolean handleResponse(MessageContext messageContext, Object endpoint) throws Exception {
        try {
            SoapAuditLog auditLog = soapAuditLog.get();
            if (auditLog != null) {
                // Capturar response payload
                auditLog.setResponsePayload(extractPayload(messageContext.getResponse()));
                auditLog.setStatus(RequestStatus.SUCCESS);
            }
            return true;
        } catch (Exception e) {
            log.error("Error in handleResponse interceptor", e);
            return true;
        }
    }

    @Override
    public boolean handleFault(MessageContext messageContext, Object endpoint) throws Exception {
        try {
            SoapAuditLog auditLog = soapAuditLog.get();
            if (auditLog != null) {
                auditLog.setStatus(RequestStatus.FAULT);
                auditLog.setResponsePayload(extractPayload(messageContext.getResponse()));

                // Extraer mensaje de error del SOAP Fault
                if (messageContext.getResponse() instanceof SoapMessage soapResponse &&
                        soapResponse.getSoapBody() != null && soapResponse.getSoapBody().getFault() != null) {
                    auditLog.setErrorMessage(soapResponse.getSoapBody().getFault().getFaultStringOrReason());
                }

            }
            return true;
        } catch (Exception e) {
            log.error("Error in handleFault interceptor", e);
            return true;
        }
    }

    @Override
    public void afterCompletion(MessageContext messageContext, Object endpoint, Exception ex) {
        try {
            SoapAuditLog auditLog = soapAuditLog.get();
            if (auditLog != null) {
                // Calcular duración
                auditLog.setDurationMs(System.currentTimeMillis() - auditLog.getStartTime());

                // Si hay excepción, marcar como error
                if (ex != null) {
                    auditLog.setStatus(RequestStatus.ERROR);
                    auditLog.setErrorMessage(ex.getMessage());
                }

                // Guardar en base de datos de forma asíncrona
                auditService.saveAuditLog(auditLog);

                log.info("SOAP Request completed - Method: {}, IP: {}, Duration: {}ms, Status: {}",
                        auditLog.getSoapMethod(),
                        auditLog.getIpOrigin(),
                        auditLog.getDurationMs(),
                        auditLog.getStatus());
            }
        } catch (Exception e) {
            log.error("Error saving audit log", e);
        } finally {
            // Limpiar ThreadLocal
            soapAuditLog.remove();
        }
    }

    private String extractClientIp() {
        HttpServletRequest request = ((ServletRequestAttributes)
                RequestContextHolder.currentRequestAttributes()).getRequest();

        // Verificar headers de proxy
        String[] headers = {
                "X-Forwarded-For",
                "X-Real-IP",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP",
                "HTTP_X_FORWARDED_FOR"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // Si hay múltiples IPs, tomar la primera
                return ip.split(",")[0].trim();
            }
        }

        return request.getRemoteAddr();
    }

    private void extractSoapInfo(MessageContext messageContext, SoapAuditLog soapAuditLog, Object endpoint) {
        if (messageContext.getRequest() instanceof SoapMessage soapRequest && soapRequest.getSoapBody() != null) {
                Source bodySource = soapRequest.getSoapBody().getPayloadSource();
                if (bodySource instanceof DOMSource domSource) {
                    Node rootNode = domSource.getNode();
                    if (rootNode != null) {
                        soapAuditLog.setSoapMethod(rootNode.getLocalName());
                    }
                }
            }


        // Información del endpoint
        if (endpoint != null) {
            Method method = findHandlerMethod(endpoint);
            if (method != null) {
                soapAuditLog.setEndpointClass(method.getDeclaringClass().getSimpleName());
            }
        }
    }

    private String extractPayload(WebServiceMessage message) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            message.writeTo(outputStream);
            String payload = outputStream.toString(StandardCharsets.UTF_8);

            // Limitar tamaño si es necesario
            if (payload.length() > 10000) {
                return payload.substring(0, 10000) + "... [TRUNCATED]";
            }

            return payload;
        } catch (Exception e) {
            log.error("Error extracting payload", e);
            return "Error extracting payload: " + e.getMessage();
        }
    }

    private Method findHandlerMethod(Object endpoint) {
        if (endpoint instanceof MethodEndpoint methodEndpoint) {
            return methodEndpoint.getMethod();
        }
        return null;
    }
}