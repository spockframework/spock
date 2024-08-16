import java.time.Duration

runner {
  parallel {
    enabled true
  }
}

timeout {
  globalTimeout Duration.ofMinutes(1)
  maxInterruptAttemptsWithThreadDumps 1
  printThreadDumpsOnInterruptAttempts true
}
