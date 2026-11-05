# Emails

Outbound mail uses **Jakarta Mail** under the hood. The fluent **`Mail`** builder composes messages, and **`PostOffice`** sends them asynchronously on a virtual thread, so your controller can return immediately after calling `send()` instead of waiting on the SMTP round trip.

Templates for HTML or plain text live under `src/main/resources/templates/` and go through the same Freemarker engine as regular HTTP responses. SMTP settings and credentials come from `config.yaml`, and passwords should use `env{}` or `file{}` rather than sit in the file as plain text. For tests, `SmtpMock` in `mangooio-test` captures messages without needing a real mail server running.

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

The one-argument `from(String)` accepts a bare address or the `Name <email>` format. Without `from()` the mail is sent with the default sender from `smtp.from` (`mangoo <noreply@mangoo.local>` unless configured), which may use the `Name <email>` format. Any failure while sending is logged, as sending runs on a virtual thread nobody waits for.

## Templates

Render Freemarker by passing a template path and a content map. Both template methods throw the checked `MangooTemplateEngineException`. `send()` itself only logs SMTP failures.

```java
try {
    Mail.newMail()
        .from("noreply@example.com")
        .to("user@example.com")
        .subject("Welcome")
        .htmlMessage("emails/welcome.ftl", Map.of("name", "Ada"))
        .send();
} catch (MangooTemplateEngineException e) {
    // template missing or the render failed
}
```

`textMessage(template, content)` renders the same way. The template engine's output format is HTML, so interpolations are escaped unless the template starts with `<#ftl output_format="plainText">`. Templates are loaded from the classpath under `/templates/`. Session, flash, and the other web variables are not in this context. Pass everything the template needs in the map.

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

Priority ranges from 1 (highest) to 5 (lowest) and is sent as `X-Priority`. An attachment whose length is 0 throws `IllegalArgumentException`. `attachments(List<Path>)` adds several files.

HTML parts are `text/html; charset=utf-8`. A plain-text part uses JavaMail's default charset. Template source and output encoding are UTF-8, which is separate from that MIME charset.

SMTP settings live under `smtp.*` in [Configuration](configuration.md). `smtp.protocol: smtps` opens TLS before any SMTP command, and `smtptls` aborts the delivery when the server does not complete STARTTLS. `smtp` is cleartext and is accepted only in dev and test, which is what `SmtpMock` needs. A connection or a read that exceeds `smtp.connection.timeout` or `smtp.read.timeout` (10 seconds and 30 seconds by default) is aborted and logged with the configured limits. For tests, reach for `io.mangoo.test.email.SmtpMock` from `mangooio-test` instead of a real SMTP server.
