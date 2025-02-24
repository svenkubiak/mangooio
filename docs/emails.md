# Sending Emails with mangoo I/O

mangoo I/O leverages **Jakarta Mail** to facilitate email sending. Below is an example of how to send an email:

```java
Mail.newMail()
        .from("Jon Snow <jon.snow@winterfell.com>")
        .to("sansa.stark@westeros.com")
        .subject("Lord of Light")
        .textMessage("What is dead may never die")
        .send();
```

## Important Notes

- Messages that use templates are automatically set to **HTML format**.
- Default email encoding is **UTF-8**.
- Variables like `Session`, `Flash`, and others that are available in standard templates **are not available** when sending emails. To include such data, pass them explicitly using the `withContent` method.
