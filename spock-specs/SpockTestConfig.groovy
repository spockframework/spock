import java.time.Duration

runner {
  parallel {
    enabled true
  }
  filterStackTrace false
}

timeout {
  globalTimeout Duration.ofMinutes(1)
  applyGlobalTimeoutToFixtures true
  maxInterruptAttemptsWithThreadDumps 1
  printThreadDumpsOnInterruptAttempts true
}
