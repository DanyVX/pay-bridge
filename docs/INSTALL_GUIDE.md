# Installing and using PayBridge

This guide is for the shopkeeper using PayBridge day-to-day — not a developer. If something here
doesn't match what you see on your phone, menu names and layouts vary a little between phone
brands and Android versions.

## What this app does

PayBridge checks that a payment from a customer really arrived, by reading the payment
confirmation your own phone already receives from NayaPay, Easypaisa, or JazzCash — the same
notification you'd see if you checked those apps yourself. It never asks the customer for
anything, and it can't be fooled by a fake screenshot, because it's reading directly from your
phone, not from what the customer shows you.

## Why it's not from the Play Store

PayBridge is installed directly (as a file), not through the Google Play Store. This is because
the Play Store is very strict about apps that read notifications, even when — like here — the app
only reads three specific payment apps and never sends anything anywhere. Installing it directly
is completely normal and safe as long as you got the file from someone you trust (whoever set
this up for you).

## Step 1 — Install the app

1. You'll receive a file ending in `.apk` (for example, sent over WhatsApp or copied by USB).
2. Open the file. Your phone will likely show a warning like "Install blocked" or "For your
   security, your phone is not allowed to install unknown apps from this source."
3. Tap **Settings** on that warning, and turn on **Allow from this source** (wording varies by
   phone) for the app you used to open the file (e.g. WhatsApp or your Files app).
4. Go back and open the `.apk` file again, then tap **Install**.

## Step 2 — Turn on Notification Access

When you first open PayBridge, it will explain what it does and ask you to turn on
**Notification Access**. This is a one-time step:

1. Tap **Continue** on the first screen, then **Open Notification Access Settings**.
2. Find **PayBridge** in the list Android shows you, and turn it on.
3. Confirm if Android asks "Allow PayBridge to read notifications?"
4. Go back to PayBridge — it will show **Status: GRANTED** once this worked.

## Step 3 — Make sure your phone won't shut PayBridge down in the background

Many phones (especially Xiaomi/Redmi, Vivo, and Oppo) aggressively close apps running in the
background to save battery — which can silently stop PayBridge from working even after you've
granted notification access. Do this once, right after installing:

**Xiaomi / Redmi / POCO (MIUI/HyperOS):**
Settings → Apps → Permissions → Background autostart → find PayBridge → turn it **on**.
Also check Settings → Battery → App battery saver, and set PayBridge to **No restrictions**.

**Vivo:**
Settings → Battery → *High background power consumption* (older Vivo phones) or *Background
power consumption management* (Android 12 and newer) → find PayBridge → allow it to run in the
background / choose **Don't restrict**.

**Oppo:**
Settings → Battery and Storage → Battery Manager → Power consumption details → find PayBridge and
turn **off** "Optimize for excessive power consumption" for it. You can also open the **Phone
Manager** app and check PayBridge isn't listed under auto-launch restrictions.

**Any phone (general Android):** if PayBridge ever shows "Listener: NOT RUNNING" on its home
screen, tap that message — it takes you straight to the right Settings screen to fix it.

## Step 4 — Using it day-to-day

1. When a customer is about to pay, open PayBridge and tap **+ New Expected Payment**.
2. Enter the amount you're expecting (e.g. `1500` for Rs. 1,500). You can leave "Provider" as
   **Any** if you don't know which app they'll use, or pick one if you do.
3. Tap **Start Waiting**.
4. Once the customer pays, PayBridge will show one of these stamps:
   - 🟩 **PAYMENT RECEIVED** — the money arrived and matches the amount. Safe to hand over goods.
   - 🟥 **TIMED OUT — not confirmed** — nothing matching arrived in time. Do not release goods
     without checking another way.
   - 🟧 **MISMATCH** — a payment arrived, but for a different amount than expected. Check with
     the customer before proceeding.
   - ⬜ **COULD NOT VERIFY — listener was inactive** — PayBridge could not check at all, because
     its notification access was off or the phone put it to sleep during the wait. This is
     different from "not received" — it means PayBridge genuinely doesn't know, not that the
     money didn't arrive. Ask the customer to show their own app, or check NayaPay/Easypaisa/
     JazzCash directly, and fix the listener status shown on the home screen afterward.
5. Every attempt (successful or not) is saved in **History**, so you can look back at it later.

## Things to know

- **Everything stays on this phone.** PayBridge never sends anything to the internet.
- **If you uninstall the app or change phones, your history is gone.** It's only ever stored on
  this device — there is no backup or account to restore from.
- If the **Listener** banner at the top of the home screen ever says **NOT RUNNING**, tap it and
  turn Notification Access back on — until you do, PayBridge cannot verify any payment.
