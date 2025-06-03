package com.elearning.remoteensine.service;

import com.elearning.remoteensine.dao.NewsletterDAO;
import com.elearning.remoteensine.model.NewsLetterInscription;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class NewsletterService {

  private NewsletterDAO newsletterDAO;
  private static final Pattern EMAIL_PATTERN = Pattern.compile(
      "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");


  public NewsletterService(NewsletterDAO newsletterDAO) {
    this.newsletterDAO = newsletterDAO;
  }

  /**
   * Inscreve um novo e-mail na newsletter.
   *
   * @param email O e-mail a ser inscrito.
   * @return A inscrição criada.
   * @throws SQLException Se ocorrer erro no banco.
   * @throws IllegalArgumentException Se o e-mail for inválido ou já estiver inscrito e ativo.
   */
  public NewsLetterInscription createEmail(String email) throws SQLException, IllegalArgumentException {
    if (email == null || email.trim().isEmpty() || !EMAIL_PATTERN.matcher(email).matches()) {
      throw new IllegalArgumentException("Invalid email format.");
    }

    NewsLetterInscription existent = newsletterDAO.searchByEmail(email);
    if (existent != null) {
      if (existent.isActive()) {
        throw new IllegalArgumentException("This email is already subscribed and active in the newsletter.");
      } else {
        existent.setActive(true);
        newsletterDAO.updateInscriptionStatus(existent);
        return existent;
      }
    }

    NewsLetterInscription newInscription = new NewsLetterInscription(email.trim());
    return newsletterDAO.saveInscription(newInscription);
  }

  /**
   * Cancela a inscrição de um e-mail na newsletter.
   *
   * @param email O e-mail a ser descadastrado.
   * @return true se a inscrição foi desativada com sucesso.
   * @throws SQLException Se ocorrer erro no banco.
   * @throws IllegalArgumentException Se o e-mail não for encontrado ou já estiver inativo.
   */
  public boolean cancelInscription(String email) throws SQLException, IllegalArgumentException {
    if (email == null || email.trim().isEmpty()) {
      throw new IllegalArgumentException("Email cannot be empty to unsubscribe.");
    }

    NewsLetterInscription inscription = newsletterDAO.searchByEmail(email);
    if (inscription == null) {
      throw new IllegalArgumentException("\n" +
          "Email not found in newsletter list.");
    }

    if (!inscription.isActive()) {
        System.out.println("E-mail " + email + " was already unregistered.");
      return true;
    }

    inscription.setActive(false);
    return newsletterDAO.updateInscriptionStatus(inscription);
  }

  /**
   * Verifica o status da inscrição de um e-mail na newsletter.
   *
   * @param email O e-mail a ser verificado.
   * @return true se o e-mail está inscrito e ativo, false caso contrário.
   * @throws SQLException Se ocorrer erro no banco.
   */
  public boolean checkInscriptionStatus(String email) throws SQLException {
    if (email == null || email.trim().isEmpty()) {
      return false;
    }
    NewsLetterInscription inscription = newsletterDAO.searchByEmail(email);
    return inscription != null && inscription.isActive();
  }

  /**
   * Lista todos os e-mails inscritos (apenas os ativos).
   * @return Lista de e-mails ativos.
   * @throws SQLException Erro de banco.
   */
  public List<String> listAllActivesEmails() throws SQLException {
    List<NewsLetterInscription> inscriptions = newsletterDAO.listAllInscriptions(true);
    List<String> emails = new java.util.ArrayList<>();
    for (NewsLetterInscription inscription : inscriptions) {
      emails.add(inscription.getEmail());
    }
    return emails;
  }

  /**
   * Lista todas as inscrições (para fins administrativos, por exemplo).
   * @return Lista de todas as NewsletterInscricao.
   * @throws SQLException Erro de banco.
   */
  public List<NewsLetterInscription> getAllInscriptions() throws SQLException {
    return newsletterDAO.listAllInscriptions(false);
  }
}

