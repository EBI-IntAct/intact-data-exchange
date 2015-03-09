/**
 * Copyright 2009 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.dataexchange.psimi.exporter.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

/**
 */
public class MailNotifierStepExecutionListener implements StepExecutionListener {

    protected static final Log log = LogFactory.getLog(MailNotifierStepExecutionListener.class);

    private MailSender mailSender;
    private String senderAddress;

    public MailNotifierStepExecutionListener() {

    }

    public void beforeStep(StepExecution stepExecution) {

        SimpleMailMessage message = newSimpleMessage();
        message.setSubject("[IntAct_export] Started step: "+stepExecution.getStepName()+"");
        message.setText(stepExecution.getSummary()+"\n"+stepExecution.getJobExecution());
        message.setTo(stepExecution.getJobParameters().getString("email.recipient"));

        try{
            mailSender.send(message);
        }
        catch (MailException e){
            log.error("Impossible to send e-mail", e);
        }
    }

    public ExitStatus afterStep(StepExecution stepExecution) {

        SimpleMailMessage message = newSimpleMessage();
        message.setSubject("[IntAct_export] Finished step: "+stepExecution.getStepName()+" Exit status: "+stepExecution.getExitStatus().getExitCode());
        message.setText(stepExecution.toString()+"\n"+stepExecution.getExecutionContext());
        message.setText(stepExecution.toString()+"\n"+stepExecution.getSummary()+"\n"+stepExecution.getJobExecution());
        message.setTo(stepExecution.getJobParameters().getString("email.recipient"));

        try{
            mailSender.send(message);
        }
        catch (MailException e){
            log.error("Impossible to send e-mail", e);
        }

        return stepExecution.getExitStatus();
    }

    private SimpleMailMessage newSimpleMessage() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderAddress);
        return message;
    }

    public void setMailSender(MailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void setSenderAddress(String senderAddress) {
        this.senderAddress = senderAddress;
    }
}
