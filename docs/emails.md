# Emails

Outbound mail uses **Jakarta Mail** under the hood. The fluent **`Mail`** builder composes messages, and **`PostOffice`** sends them asynchronously on a virtual thread, so your controller can return immediately after calling `send()` instead of waiting on the SMTP round trip.

Templates for HTML or plain text live under `src/main/resources/templates/` and go through the same Freemarker engine as regular HTTP responses. SMTP settings and credentials come from `config.yaml`, and passwords should use `vault{}` rather than sit in the file as plain text. For tests, `SmtpMock` in `mangooio-test` captures messages without needing a real mail server running.

Basic send:

```java
Mail.newMail()
    .from("noreply@winterfell.example")
    .to("sansa@westeros.example")
    .subject("Lord of Light")
    .textMessage("What is dead may never die")
    .send();
```

Set a display name with the two-argument `from`:

```java
Mail.newMail()
    .from("Jon Snow", "jon@winterfell.example")
    .to("sansa@westeros.example")
    .subject("Lord of Light")
    .htmlMessage("<p>What is dead may never die</p>")
    .send();
```

The one-argument `from(String)` sets the address only; it does not parse a `Name <email>` string for you.

## Templates

Render Freemarker by passing a template path and a content map instead of a literal string. Template messages are sent as HTML when you call `htmlMessage`:

```java
Mail.newMail()
    .from("noreply@example.com")
    .to("user@example.com")
    .subject("Welcome")
    .htmlMessage("emails/welcome.ftl", Map.of("name", "Ada"))
    .send();
```

`textMessage(template, content)` renders the same way, just as plain text. Session, Flash, and the other web template variables are **not** present in this context, since there is no request to draw them from, so pass everything the template needs in the map yourself.

## Other helpers

```java
Mail.newMail()
    .from("Ada", "ada@example.com")
    .to("one@example.com", "two@example.com")
    .cc("cc@example.com")
    .bcc("bcc@example.com")
    .replyTo("support@example.com")
    .header("X-Campaign", "welcome")
    .priority(1)
    .attachment(Path.of("/tmp/invoice.pdf"))
    .textMessage("See attachment")
    .send();
```

Priority ranges from 1 (highest) to 5 (lowest), and encoding is always UTF-8.

SMTP settings live under `smtp.*` in [Configuration](configuration.md). For tests, reach for `io.mangoo.test.email.SmtpMock` from `mangooio-test` instead of a real SMTP server.
