import org.spockframework.specs.extension.Snapshot

runner {
  parallel {
    enabled true
  }
  if (snapshots.updateSnapshots) {
    include Snapshot
  }
}
