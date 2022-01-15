// The script generates a random subset of valid jdk, os, timezone, and other axes.
// You can preview the results by running "node matrix.js"
// See https://github.com/vlsi/github-actions-random-matrix
let {MatrixBuilder} = require('./matrix_builder');
const matrix = new MatrixBuilder();
matrix.addAxis({
  name: 'java_distribution',
  values: [
    'zulu',
    'temurin',
    'liberica',
    'microsoft',
  ]
});

matrix.addAxis({
  name: 'java8_distribution',
  values: [
    'zulu',
    'temurin',
    'liberica',
  ]
});

// TODO: support different JITs (see https://github.com/actions/setup-java/issues/279)
matrix.addAxis({name: 'jit', title: '', values: ['hotspot']});

matrix.addAxis({
  name: 'java',
  // Strings allow versions like 18-ea
  values: [
    '8',
    '11',
    '17',
  ]
});

matrix.addAxis({
  name: 'tz',
  values: [
    'America/New_York',
    'Pacific/Chatham',
    'UTC'
  ]
});

matrix.addAxis({
  name: 'os',
  title: x => x.replace('-latest', ''),
  values: [
    'ubuntu-latest',
    'windows-latest',
    'macos-latest'
  ]
});

matrix.addAxis({
  name: 'variant',
  values: [
    '2.5',
    '3.0',
  ]
});

// Test cases when Object#hashCode produces the same results
// It allows capturing cases when the code uses hashCode as a unique identifier
matrix.addAxis({
  name: 'hash',
  values: [
    {value: 'regular', title: '', weight: 42},
    {value: 'same', title: 'same hashcode', weight: 1}
  ]
});
matrix.addAxis({
  name: 'locale',
  title: x => x.language + '_' + x.country,
  values: [
    {language: 'de', country: 'DE'},
    {language: 'fr', country: 'FR'},
    // TODO: fix :src:dist-check:batchBUG_62847
    // Fails with "ERROR o.a.j.u.JMeterUtils: Could not find resources for 'ru_EN'"
    // {language: 'ru', country: 'RU'},
    {language: 'tr', country: 'TR'},
  ]
});

matrix.setNamePattern(['java', 'java_distribution', 'hash', 'os', 'tz', 'locale']);

// Microsoft Java has no distribution for 8
matrix.exclude({java_distribution: 'microsoft', java: 8});
matrix.exclude({java: 17, variant: '2.5', os: 'ubuntu-latest'});
// Ensure at least one job with "same" hashcode exists
matrix.generateRow({hash: {value: 'same'}});
// Ensure at least one Windows and at least one Linux job is present (macOS is almost the same as Linux)
matrix.generateRow({os: 'windows-latest'});
matrix.generateRow({os: 'ubuntu-latest'});
// Ensure there will be at least one job with Java 8
matrix.generateRow({java: 8});
// Ensure there will be at least one job with Java 11
matrix.generateRow({java: 11});
// Ensure there will be at least one job with Java 17
matrix.generateRow({java: 17});
const include = matrix.generateRows(process.env.MATRIX_JOBS || 5);
if (include.length === 0) {
  throw new Error('Matrix list is empty');
}
include.sort((a, b) => a.name.localeCompare(b.name, undefined, {numeric: true}));
include.forEach(v => {
  let jvmArgs = [];
  if (v.hash.value === 'same') {
    jvmArgs.push('-XX:+UnlockExperimentalVMOptions', '-XX:hashCode=2');
  }
  // Gradle does not work in tr_TR locale, so pass locale to test only: https://github.com/gradle/gradle/issues/17361
  jvmArgs.push(`-Duser.country=${v.locale.country}`);
  jvmArgs.push(`-Duser.language=${v.locale.language}`);
  if (v.jit === 'hotspot' && Math.random() > 0.5) {
    // The following options randomize instruction selection in JIT compiler
    // so it might reveal missing synchronization in TestNG code
    v.name += ', stress JIT';
    jvmArgs.push('-XX:+UnlockDiagnosticVMOptions');
    if (v.java >= 8) {
      // Randomize instruction scheduling in GCM
      // share/opto/c2_globals.hpp
      jvmArgs.push('-XX:+StressGCM');
      // Randomize instruction scheduling in LCM
      // share/opto/c2_globals.hpp
      jvmArgs.push('-XX:+StressLCM');
    }
    /*
    TODO: gradle is launched with Java 8 always, so we should pass the options via test task execution
    if (v.java >= 16) {
      // Randomize worklist traversal in IGVN
      // share/opto/c2_globals.hpp
      jvmArgs.push('-XX:+StressIGVN');
    }
    if (v.java >= 17) {
      // Randomize worklist traversal in CCP
      // share/opto/c2_globals.hpp
      jvmArgs.push('-XX:+StressCCP');
    }
    */
  }
  v.testExtraJvmArgs = jvmArgs.join(' ');
  delete v.hash;
});

console.log(include);
console.log('::set-output name=matrix::' + JSON.stringify({include}));
