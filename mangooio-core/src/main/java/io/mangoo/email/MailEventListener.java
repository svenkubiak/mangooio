package io.mangoo.email;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import biz.gabrys.lesscss.compiler2.util.StringUtils;
import io.mangoo.core.Config;
import io.mangoo.enums.Required;
import jodd.mail.Email;
import jodd.mail.MailServer;
import jodd.mail.MailServer.Builder;
import jodd.mail.SmtpServer;
import jodd.mail.SmtpSslServer;

/**
 * 
 * @author svenkubiak
 *
 */
@Singleton
public class MailEventListener {
    private SmtpServer smtpServer;
    private SmtpSslServer smtpSslServer;
    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    
    @Inject
    public MailEventListener(Config config) {
        Objects.requireNonNull(config, Required.CONFIG.toString());
        
        Builder builder = MailServer.create()
                .debugMode(config.isSmtpDebug())
                .host(config.getSmtpHost())
                .port(config.getSmtpPort());
        
        if (StringUtils.isNotBlank(config.getSmtpUsername()) && StringUtils.isNotBlank(config.getSmtpPassword())) {
            builder.auth(config.getSmtpUsername(), config.getSmtpPassword());
        }
        
        if (config.isSmtpSSL()) {
            builder.ssl(true);
            this.smtpSslServer = new SmtpSslServer(builder);
            this.smtpSslServer.plaintextOverTLS(config.isSmtpPlaintextOverTLS());
            this.smtpSslServer.startTlsRequired(config.isSmtpStartTlsRequired());
        } else {
            this.smtpServer = builder.buildSmtpMailServer(); 
        }
    }
    
    @Subscribe
    public void listen(Email email) {
        Objects.requireNonNull(email, Required.EMAIL.toString());
        executor.execute(new MailExecutor(this.smtpSslServer, this.smtpServer, email));
    }
}