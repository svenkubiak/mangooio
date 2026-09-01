# Emails

mangoo I/O sends mail through Jakarta Mail. `Mail.send()` dispatches on a virtual thread via `PostOffice`.

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

The one-argument `from(String)` sets the address only. It does not parse `Name <email>`.

## Templates

Render Freemarker by passing a template path and a content map. Template messages are sent as HTML when you call `htmlMessage`:

```java
Mail.newMail()
    .from("noreply@example.com")
    .to("user@example.com")
    .subject("Welcome")
    .htmlMessage("emails/welcome.ftl", Map.of("name", "Ada"))
    .send();
```

`textMessage(template, content)` renders the same way as plain text. Session, Flash, and the other web template variables are **not** present. Pass everything you need in the map.

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

Priority is 1 (highest) to 5 (lowest). Encoding is UTF-8.

SMTP settings live under `smtp.*` in [Configuration](configuration.md). For tests, use `io.mangoo.test.email.SmtpMock` from `mangooio-test`.
