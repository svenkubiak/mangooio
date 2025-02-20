mangoo I/O uses [Jodd Email](https://jodd.org/email/) to make sending eMails as easy as possible. If you want to send an eMail via mangoo I/O you need the Mail utility class. See the following example:

```java
Mail.build()
    .withBuilder(Email.create()
                 .from("Jon Snow <jon.snow@winterfell.com>")
                 .to("sansa.stark@winterfell.com")
                 .subject("Lord of light"))
    .templateMessage("emails/simple.ftl", content)
    .send();
```

Please note, that variables like Session, Flash, etc. which are automatically availabie in the normal template mechanism, are **not** available when sending emails. You have to pass them via the withContent method.

All messages using a template will be set to HTML automatically.

You can also send messages using plain text body. See the following example:

```java
Mail.build()
    .withBuilder(Email.create()
                 .from("Jon Snow <jon.snow@winterfell.com>")
                 .to("sansa.stark@westeros.com")
                 .subject("Lord of light")
                 .textMessage("what is dead may never die"))
    .send();
```

** E-Mails with attachment **

You can als send messages with an attachment. See the following example:

```java
Mail.build()
    .withBuilder(Email.create()
                 .from("Jon Snow <jon.snow@winterfell.com>")
                 .to("sansa.stark@westeros.com")
                 .subject("Lord of light")
                 .textMessage("what is dead may never die")
                 .attachment(EmailAttachment.with()
                             .name("some name")
                             .content(file))
             	)
    .send();
```

Default encoding for eMails is UTF-8.

#### Configuring the SMTP connection

Check the [default values](https://docs.mangoo.io/default-values.html) for the SMTP server on how to configure your SMTP server connection correctly.

#### Mocked SMTP server
For testing and development purposes, mangoo I/O provides a simple mocked SMTP server which is based on [GreenMail](http://www.icegreen.com/greenmail/).

Simply inject the SMTP server

```java
@Inject
private SmtpMock smtpMock;
```

And start anywhere you want

```java
smtpMock.start();
```

The mocked SMTP server uses the configuration within config.props for host and port of the SMTP server and the start method only has an affect in dev and test mode. 

Please note, that the SmtpMock class is part of the mangooio-test maven artifact.