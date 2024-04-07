package com.hello.money.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import redis.embedded.RedisServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


@TestConfiguration
public class EmbeddedRedisConfig {

  @Value("${spring.redis.data.port}")
  private int redisPort;

  private RedisServer redisServer;

  @PostConstruct
  public void postConstruct() throws IOException {
    int port = isRedisRunning() ? findAvailablePort() : redisPort;
    redisServer = new RedisServer(port);
    redisServer.start();
  }

  @PreDestroy
  public void preDestroy() throws IOException {
    if (redisServer != null) {
      redisServer.stop();
    }
  }

  /**
   * Embedded Redis가 현재 실행중인지 확인
   * 맥/리눅스 가능
   * 윈도우 불가능
   */
  private boolean isRedisRunning() throws IOException {
    return isRunning(executeGrepProcessCommand(redisPort));
  }

  /**
   * 현재 PC/서버에서 사용가능한 포트 조회
   */
  private int findAvailablePort() throws IOException {
    for (int port = 10000; port <= 65535; port++) {
      final Process process = executeGrepProcessCommand(port);
      if (!isRunning(process)) {
        return port;
      }
    }
    throw new IllegalArgumentException("Not Found Available port: 10000 ~ 65535");
  }

  /**
   * 해당 port를 사용중인 프로세스 확인하는 sh 실행
   */
  private Process executeGrepProcessCommand(final int port) throws IOException {
    String command = String.format("netstat -nat | grep LISTEN|grep %d", port);
    String[] shell = {"/bin/sh", "-c", command};
    return Runtime.getRuntime().exec(shell);
  }

  /**
   * 해당 Process가 현재 실행중인지 확인
   */
  private boolean isRunning(final Process process) {
    String line;
    final StringBuilder pidInfo = new StringBuilder();

    try (BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
      while ((line = input.readLine()) != null) {
        pidInfo.append(line);
      }
    } catch (Exception e) {
    }
    return !StringUtils.isBlank(pidInfo.toString());
  }
}
