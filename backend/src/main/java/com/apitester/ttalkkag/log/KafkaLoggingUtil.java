package com.apitester.ttalkkag.log;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@AllArgsConstructor
public class KafkaLoggingUtil {
    private static final Logger TLO_LOGGER = LoggerFactory.getLogger("com.apitester.ttalkkag.log.TLO");
    private final UrlMappingResolver urlMappingResolver;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    public void logKafkaTLO(String logKey, String topic) {
        String logTime = LocalDateTime.now().format(formatter);

        Tlo tlo = new Tlo();
        Optional<UrlMapping> mapping = urlMappingResolver.resolveMapping(topic, ""); // GET/POST 구분 없을 경우 빈 문자열

        mapping.ifPresent(m -> {
            tlo.setFuncId(m.getFuncId());
            tlo.setMid(m.getMid());
        });

        tlo.setSeqId(logKey);
        tlo.setLogTime(logTime);
        tlo.setReqTime(logTime);
        tlo.setRspTime(logTime);
        tlo.setLogType("SVC");
        tlo.setSid("");
        tlo.setDevInfo("Chrome");
        tlo.setNwInfo("ETC");
        tlo.setSvcName("ttalkkag");
        tlo.setDevModel("");
        tlo.setCarrierType("E");
        tlo.setUrl("KAFKA://" + topic);
        tlo.setClientIp("127.0.0.1");
        tlo.setLogKey(logKey);
        tlo.setChannelType("IN"); // 고정 값
        tlo.setResultCode("20000000");
        tlo.setChannelType("IN");

        TLO_LOGGER.info(tlo.toString());
    }
}
