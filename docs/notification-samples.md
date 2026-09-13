# Notification samples — DO NOT INVENT, fill only with real observed data

This file exists so the three parser stubs (`NayaPayParser`, `EasypaisaParser`, `JazzCashParser`)
can be filled in from real notification text instead of a guessed format. Every field below is
a blank placeholder on purpose — leave anything you haven't personally observed as
`<PASTE REAL SAMPLE HERE>` rather than filling in something "plausible."

## How to capture a sample

1. Make (or receive) a real payment of a known amount into the account whose app is installed
   on the test phone.
2. When the notification arrives, use a notification-inspection tool (e.g. a debug build of
   this app logging `NotificationPayload`, or a general-purpose "notification log" app) to
   capture the exact package name, title, short text (`EXTRA_TEXT`), and expanded text
   (`EXTRA_BIG_TEXT`) verbatim — do not paraphrase or clean up spacing/punctuation.
3. Repeat for at least 2-3 different amounts per provider, since providers sometimes format
   currency differently depending on whether the amount has decimals, is rounded, etc.
4. Once a provider has its samples filled in below, un-skip that provider's `@Ignore`d parser
   tests and implement the real `parse()` logic against them.

## NayaPay

### Sample 1
- Package name: `<PASTE REAL SAMPLE HERE>`
- Notification title: `<PASTE REAL SAMPLE HERE>`
- Notification text (`EXTRA_TEXT`): `<PASTE REAL SAMPLE HERE>`
- Notification bigText (`EXTRA_BIG_TEXT`), if different: `<PASTE REAL SAMPLE HERE>`
- Amount format observed (e.g. "Rs. 500" / "PKR 500.00" / "500"): `<PASTE REAL SAMPLE HERE>`
- Reference/transaction ID present? Where in the text?: `<PASTE REAL SAMPLE HERE>`
- Date/time captured: `<PASTE REAL SAMPLE HERE>`

### Sample 2 (different amount)
- Package name: `<PASTE REAL SAMPLE HERE>`
- Notification title: `<PASTE REAL SAMPLE HERE>`
- Notification text (`EXTRA_TEXT`): `<PASTE REAL SAMPLE HERE>`
- Notification bigText (`EXTRA_BIG_TEXT`), if different: `<PASTE REAL SAMPLE HERE>`
- Amount format observed: `<PASTE REAL SAMPLE HERE>`
- Reference/transaction ID present? Where in the text?: `<PASTE REAL SAMPLE HERE>`
- Date/time captured: `<PASTE REAL SAMPLE HERE>`

### Sample 3 (different amount)
- Package name: `<PASTE REAL SAMPLE HERE>`
- Notification title: `<PASTE REAL SAMPLE HERE>`
- Notification text (`EXTRA_TEXT`): `<PASTE REAL SAMPLE HERE>`
- Notification bigText (`EXTRA_BIG_TEXT`), if different: `<PASTE REAL SAMPLE HERE>`
- Amount format observed: `<PASTE REAL SAMPLE HERE>`
- Reference/transaction ID present? Where in the text?: `<PASTE REAL SAMPLE HERE>`
- Date/time captured: `<PASTE REAL SAMPLE HERE>`

## Easypaisa

### Sample 1
- Package name: `<PASTE REAL SAMPLE HERE>`
- Notification title: `<PASTE REAL SAMPLE HERE>`
- Notification text (`EXTRA_TEXT`): `<PASTE REAL SAMPLE HERE>`
- Notification bigText (`EXTRA_BIG_TEXT`), if different: `<PASTE REAL SAMPLE HERE>`
- Amount format observed: `<PASTE REAL SAMPLE HERE>`
- Reference/transaction ID present? Where in the text?: `<PASTE REAL SAMPLE HERE>`
- Date/time captured: `<PASTE REAL SAMPLE HERE>`

### Sample 2 (different amount)
- Package name: `<PASTE REAL SAMPLE HERE>`
- Notification title: `<PASTE REAL SAMPLE HERE>`
- Notification text (`EXTRA_TEXT`): `<PASTE REAL SAMPLE HERE>`
- Notification bigText (`EXTRA_BIG_TEXT`), if different: `<PASTE REAL SAMPLE HERE>`
- Amount format observed: `<PASTE REAL SAMPLE HERE>`
- Reference/transaction ID present? Where in the text?: `<PASTE REAL SAMPLE HERE>`
- Date/time captured: `<PASTE REAL SAMPLE HERE>`

### Sample 3 (different amount)
- Package name: `<PASTE REAL SAMPLE HERE>`
- Notification title: `<PASTE REAL SAMPLE HERE>`
- Notification text (`EXTRA_TEXT`): `<PASTE REAL SAMPLE HERE>`
- Notification bigText (`EXTRA_BIG_TEXT`), if different: `<PASTE REAL SAMPLE HERE>`
- Amount format observed: `<PASTE REAL SAMPLE HERE>`
- Reference/transaction ID present? Where in the text?: `<PASTE REAL SAMPLE HERE>`
- Date/time captured: `<PASTE REAL SAMPLE HERE>`

## JazzCash

### Sample 1
- Package name: `<PASTE REAL SAMPLE HERE>`
- Notification title: `<PASTE REAL SAMPLE HERE>`
- Notification text (`EXTRA_TEXT`): `<PASTE REAL SAMPLE HERE>`
- Notification bigText (`EXTRA_BIG_TEXT`), if different: `<PASTE REAL SAMPLE HERE>`
- Amount format observed: `<PASTE REAL SAMPLE HERE>`
- Reference/transaction ID present? Where in the text?: `<PASTE REAL SAMPLE HERE>`
- Date/time captured: `<PASTE REAL SAMPLE HERE>`

### Sample 2 (different amount)
- Package name: `<PASTE REAL SAMPLE HERE>`
- Notification title: `<PASTE REAL SAMPLE HERE>`
- Notification text (`EXTRA_TEXT`): `<PASTE REAL SAMPLE HERE>`
- Notification bigText (`EXTRA_BIG_TEXT`), if different: `<PASTE REAL SAMPLE HERE>`
- Amount format observed: `<PASTE REAL SAMPLE HERE>`
- Reference/transaction ID present? Where in the text?: `<PASTE REAL SAMPLE HERE>`
- Date/time captured: `<PASTE REAL SAMPLE HERE>`

### Sample 3 (different amount)
- Package name: `<PASTE REAL SAMPLE HERE>`
- Notification title: `<PASTE REAL SAMPLE HERE>`
- Notification text (`EXTRA_TEXT`): `<PASTE REAL SAMPLE HERE>`
- Notification bigText (`EXTRA_BIG_TEXT`), if different: `<PASTE REAL SAMPLE HERE>`
- Amount format observed: `<PASTE REAL SAMPLE HERE>`
- Reference/transaction ID present? Where in the text?: `<PASTE REAL SAMPLE HERE>`
- Date/time captured: `<PASTE REAL SAMPLE HERE>`

## Also needed: real package names

`PaymentAppAllowlist.kt` currently contains **placeholder** package names for all three apps,
each marked `// TODO(human): verify real package name`. Before the allowlist can be trusted,
confirm the real values with the payment apps actually installed on a test phone:

```
adb shell pm list packages | grep -i nayapay
adb shell pm list packages | grep -i easypaisa
adb shell pm list packages | grep -i jazzcash
```
