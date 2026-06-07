# Policy on AI-Assisted Contributions to Spock

We use AI tools ourselves, and we welcome AI-assisted contributions. AI is a powerful tool that can genuinely help you contribute to Spock. We only ask one thing in return: stay in control of what you submit. You should understand the existing code well enough to judge whatever an AI tool produces for you, and to stand behind it.

This policy complements our general [contribution guidelines](CONTRIBUTING.adoc).

## Why this policy exists

There is a fundamental asymmetry between writing a contribution and reviewing one. AI tools make it easy to generate large amounts of code quickly, but they do not make it any faster for us to review it. Spock is maintained by a small team with a limited review budget, and we would rather spend that attention well.

A pull request is not a finished product, it is the start of a conversation. When you open one, we commit to giving it thoughtful feedback, and we expect you to engage with us in return.

## Talk to us first: prefer issues over large pull requests

**We strongly prefer an issue or a discussion over an unsolicited pull request, especially a large one.**

Before investing time in a sizeable change, AI-assisted or not, please open an issue on the [issue tracker](https://issues.spockframework.org) or start a [discussion](https://github.com/spockframework/spock/discussions). A short conversation up front lets us:

- confirm the change fits Spock's direction and goals,
- guide the design before the code is written, and
- avoid the disappointment of a large pull request that has to be reworked, sits in the queue, or cannot be accepted at all.

Small, obvious fixes are always welcome as direct pull requests. For anything larger, talking first saves everyone time.

## What we expect from contributors

1. **Understand what you submit, and be able to explain it.** This applies to issues as well as pull requests. Use AI intentionally and stay in control, rather than acting as a relay for an agent. If you cannot explain why a change is correct and why it is the right change for Spock, we will assume you do not understand it, and the contribution will likely be closed.
2. **Be able to judge the AI's output against the existing code.** Spock relies on subtle compile-time and runtime behavior. A change can look entirely plausible and still break things in ways an AI tool will not catch. We expect you to know the surrounding code well enough to tell good output from bad.
3. **Disclose significant AI involvement.** If AI tools produced a substantial part of your contribution, please say so in the description. Incidental use, such as autocomplete or asking a chatbot for advice, does not need disclosing. Disclosure will not count against you; it simply helps us understand how these tools affect the project.
4. **Collaborate with us.** We invest real time in every review. Submissions that are generated, posted, and then abandoned waste the limited capacity we have.

## What you can expect from us

1. **Human review and human decisions.** A real maintainer will read your contribution and decide on it. We may use AI tools to help us understand a change, but a human is always responsible.
2. **Honest feedback.** We weigh every contribution on its merits, regardless of how it was produced. Our queue can be long, but we will tell you clearly what works, what needs to change, and why.
3. **A relationship, not a transaction.** Community contributions are an important part of Spock. We hope you will learn from the review process, stay involved, and keep helping us improve Spock.

## In summary

AI is a welcome tool in the hands of a contributor who understands the code and is ready to collaborate. Please talk to us before large changes, prefer issues to unsolicited pull requests, and be ready to explain and stand behind everything you submit.

We reserve the right to close contributions, and to restrict future contributions, from people who repeatedly ignore this policy or submit low-effort AI-generated content. The value of a contribution lies not only in the code it delivers, but in the understanding shared and the trust built along the way.
