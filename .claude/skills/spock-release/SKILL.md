---
name: spock-release
description: Guide a Spock release (milestone or final) from release notes to next iteration, with the human pushing at every gate.
disable-model-invocation: true
allowed-tools:
  - Read
  - Edit
  - Bash(git status:*)
  - Bash(git log:*)
  - Bash(git show:*)
  - Bash(git diff:*)
  - Bash(git describe:*)
  - Bash(git remote -v)
  - Bash(git cat-file:*)
  - Bash(git fetch:*)
  - Bash(git add:*)
  - Bash(git commit:*)
  - Bash(git tag -a:*)
  - Bash(gh run list:*)
  - Bash(gh run view:*)
  - Bash(gh run watch:*)
  - Bash(gh pr list:*)
  - Bash(./allVariants build)
  - Bash(./gradlew asciidoctor:*)
  - Bash(.claude/skills/spock-release/check-deploy.sh:*)
---

# Spock release

You prepare every commit, tag and command; the human performs every write to a remote.
Each **gate** below is a hard stop: print the exact command for the human, then wait until they confirm it ran.
Commands that write to GitHub or Maven Central (`git push`, `gh release create`, anything mutating `spockframework/spock`) belong to the human, even when a shortcut looks harmless.

Track progress as a checklist with one item per step, so the human always sees where the release stands.

## Reference

- **Remote**: `spockframework` is the upstream remote (`origin` maybe a fork); confirm with `git remote -v` and use whichever remote points at `spockframework/spock`.
- **Version**: `build.gradle` `ext { baseVersion, snapshotVersion, milestone }` drives everything.
  A milestone release is `baseVersion-M<milestone>`, a final release is `baseVersion` with `milestone = 0`.
- **Tag**: `spock-<version>`, annotated, message equal to the tag name.
  Signing comes from the human's git config (`tag.gpgSign`); a missing signature is a gate failure to report, not to work around.
- **CI**: the `Build and Release Spock` workflow (`.github/workflows/release.yaml`) runs on pushes to `master` and `spock-*` tags.
  On `master` with `snapshotVersion = false` it only builds and verifies.
  On the tag it publishes to Sonatype (`release-spock` jobs) and then publishes the docs (`publish-release-docs`).
- **Release notes**: `docs/release_notes.adoc`, newest section on top, heading `== <version> (<yyyy-mm-dd>|tbd)`.
  Sections: `=== Highlights`, `=== Breaking Changes`, `=== Misc`, followed by `Thanks to all the contributors to this release: ...`.
  Entries start with an imperative verb (`Add`, `Fix`, `Improve`, `Update`, `Remove`), wrap code in backticks, and end with `spockIssue:<n>[]` or `spockPull:<n>[]`.
  A final release that follows milestones gets a summary section `_This is a summary of the highlights of the milestone releases_` that aggregates the milestones' Highlights and Breaking Changes, and its contributor list is the union of all milestones.
- **Past releases** are the ground truth for each commit's shape: `git log --oneline --grep='^Release Spock'` and `git show` the `Prepare release notes`, `Release Spock <version>` and `Prepare next iteration` commits of the last release before editing.

## Steps

### 1. Preflight

Collect from the human: the version to release (propose one from `build.gradle`, milestone or final).

Verify, and report any failure before continuing:
- working tree clean, on `master`, `HEAD` equal to `spockframework/master` after `git fetch spockframework --tags`;
- the latest `Build and Release Spock` run on `master` is green (`gh run list -R spockframework/spock -w release.yaml -b master -L 1`);
- the tag `spock-<version>` does not exist yet;
- the GitHub milestone titled `<version>`, if one exists, has no open issues or PRs.
  Find it's number with `gh api 'repos/spockframework/spock/milestones?state=all&per_page=100' --jq '.[] | select(.title == "<version>") | [.number, .state, .open_issues] | @tsv'`, then list the open items with `gh api 'repos/spockframework/spock/issues?milestone=<number>&state=open&per_page=100' --jq '.[] | [.number, (if .pull_request then "PR" else "issue" end), .title] | @tsv'`.
  The human merges, moves to another milestone, or closes each one;
- the previous release tag (`git describe --tags --abbrev=0 --match 'spock-*'`), used as the range start below.

Done when all checks pass and the human confirmed the version.
Give a summary of the preflight results, including the previous release tag and the milestone number if one exists.

### 2. Prepare release notes

Features are squash-merged, so a feature commit's subject ends in `(#<PR>)`; commits without that suffix are infrastructure and get no entry.
Commits by bots (`renovate[bot]` and other `[bot]` authors) get no entry either.
Account for every remaining commit in `<previous tag>..HEAD`: each has an entry linking its issue or PR, in the right section.

Polish the `(tbd)` section to the conventions in Reference, keeping the meaning of each entry.

Build the contributor list from the authors and `Co-authored-by:` trailers of every non-bot commit in the range, infrastructure commits included.
Name each contributor by their real name, falling back to their GitHub username when no proper name is configured.
Git author names are unreliable (nicknames, handles), so resolve each author to their GitHub login (`gh api repos/spockframework/spock/commits/<sha> --jq .author.login`) and take the profile name (`gh api users/<login> --jq .name`).
A profile name that is itself a nickname or handle, or empty, is no proper name, so use the login instead; keep the spelling earlier lists used for returning contributors.
Show the human each name you resolved from a login or fallback.

Render the docs, which also runs the link and anchor verifier: `./gradlew asciidoctor -Dvariant=<highest variant> -DjavaVersion=<highest java>` (values from `gradle.properties`).

Show the human the diff plus the feature commits you judged to need no entry, and apply their feedback.
Then commit with the message `Prepare release notes`.

Done when the human approved the diff and the commit exists locally.

### 3. Release commit

Edit, matching the previous `Release Spock` commit:
- `build.gradle`: `snapshotVersion = false`, `milestone = <n>` (or `0` for final), `baseVersion` unchanged;
- `README.adoc`: `:spock-release-version:` and `:spock-release-date:` (today);
- `docs/release_notes.adoc`: replace `(tbd)` in the release's heading with today's date.

Commit with the message `Release Spock <version>`.

Build every variant locally with `./allVariants build`, in the background.
The script carries on after a failing variant and exits with the last variant's status, so read the output of every variant for `BUILD SUCCESSFUL`.
On a failure, diagnose it and present the fix to the human before the gate.

**Gate**: the human runs `git push spockframework master`.

Watch the workflow run for the pushed sha (`gh run list -R spockframework/spock -w release.yaml -c <sha>`, then `gh run watch <id> -R spockframework/spock --exit-status`, in the background).
On failure, fetch the failed job log (`gh run view <id> --log-failed`), diagnose, and present the fix to the human; flaky timing specs get a rerun (`gh run rerun <id> --failed`, a human command) before any fix.

Done when the run for the release commit is green.

### 4. Tag

Create the tag on the release commit: `git tag -a spock-<version> -m spock-<version> <sha>`.
Verify with `git cat-file -p spock-<version>` that it is annotated, signed and points at the release commit.
When signing fails (no agent, pinentry prompt), delete the unsigned tag if one was created and hand the `git tag` command to the human.

**Gate**: the human runs `git push spockframework spock-<version>`.

Watch the tag's workflow run as in step 3, but look it up by tag (`gh run list -R spockframework/spock -w release.yaml -b spock-<version>`): the tag and `master` share the release commit, so a lookup by sha also returns the `master` run.
Then confirm the docs are live at `https://spockframework.org/spock/docs/<version>/`.

Done when every job of the tag run is green.

### 5. Release on Maven Central

The tag run only closes the staging repository; publishing it is manual.

**Gate**: the human logs in to the Central Portal (`https://central.sonatype.com/publishing/deployments`), checks the closed `org.spockframework` deployment for every Groovy variant, and publishes it.

Then wait for the `spock-core` jars of every variant from `variantsList` in `gradle.properties` to reach Maven Central, running in the background: `.claude/skills/spock-release/check-deploy.sh <version> <variant>...`.
Syncing to Maven Central can take a while after the publishing, so let the script poll instead of reporting a failure.
It gives up after `CHECK_DEPLOY_TIMEOUT` seconds (default one hour) with exit code 2, naming the missing variant and the ones not checked yet; then ask the human to check the deployment in the Central Portal.

Done when the artifacts of every variant are visible on Maven Central.

### 6. GitHub release

Print the command for the human:

```
gh release create spock-<version> -R spockframework/spock --verify-tag \
  --title "Spock <version>" \
  --notes "<notes url>" [--prerelease]
```

The notes url is `https://spockframework.org/spock/docs/<version>/release_notes.html`; add `--prerelease` for a milestone.

**Gate**: the human runs it.

Done when `gh release view spock-<version> -R spockframework/spock` shows the release.

### 7. Close the GitHub milestone

Re-run the milestone lookup from the preflight.
Skip this step when no milestone exists or it is already closed.
When `open_issues` is not `0`, something was added since the preflight; handle it as in the preflight check before continuing.

Print the command for the human, with the release date from the release notes heading:

```
gh api -X PATCH repos/spockframework/spock/milestones/<number> \
  -f state=closed -f due_on=<yyyy-mm-dd>T00:00:00Z
```

**Gate**: the human runs it.

Done when the milestone shows `closed` with the release date as `due_on`, or the step was skipped.

### 8. Prepare next iteration

Edit:
- `build.gradle`: `snapshotVersion = true`, `milestone = 0`; after a final release also bump `baseVersion` to the next minor (confirm with the human);
- `README.adoc`: `:spock-snapshot-version:` to the new `baseVersion` (past iterations forgot this, so it is a deliberate addition);
- `docs/release_notes.adoc`: after a final release, a `== <next version> (tbd)` heading on top; after a milestone, a `== <baseVersion> (tbd)` section for the upcoming final release, reusing the one already there, so the next PRs have a place for their entries.

Commit with the message `Prepare next iteration`.

**Gate**: the human runs `git push spockframework master`.

Done when the push landed and the snapshot run on `master` is green.

### 9. Announce on socials

Draft a post announcing the release, short enough for the strictest platform (280 characters including the link):
- the release name, `Spock <version>`, marked as a milestone when it is one;
- one to three highlights taken from the release's `Highlights` and `Breaking Changes` sections;
- the release notes link, `https://spockframework.org/spock/docs/<version>/release_notes.html`.

Show the human the draft and apply their feedback.

**Gate**: the human posts it.

Done when the human confirmed the post is published.
