// Travail fait par :
// Pierrick BOBET - 17 131 792
// Rémy BOUTELOUP - 17 132 265

package tp2;

import java.io.*;
import java.util.StringTokenizer;
import java.sql.*;

/**
 * Fichier de base pour le TP2 du cours IFT287
 *
 * <pre>
 * 
 * Vincent Ducharme
 * Universite de Sherbrooke
 * Version 1.0 - 7 juillet 2016
 * IFT287 - Exploitation de BD relationnelles et OO
 * 
 * Ce programme permet d'appeler des transactions d'un systeme
 * de gestion utilisant une base de donnees.
 *
 * Paramètres du programme
 * 0- site du serveur SQL ("local" ou "dinf")
 * 1- nom de la BD
 * 2- user id pour etablir une connexion avec le serveur SQL
 * 3- mot de passe pour le user id
 * 4- fichier de transaction [optionnel]
 *           si non spécifié, les transactions sont lues au
 *           clavier (System.in)
 *
 * Pré-condition
 *   - La base de donnees doit exister
 *
 * Post-condition
 *   - Le programme effectue les mises à jour associees à chaque
 *     transaction
 * </pre>
 */
public class Devoir2
{
    private static Connexion cx;

    private static PreparedStatement stmtSelectProcesNonTermine;
    private static PreparedStatement stmtExisteProcesDansSeance;
    private static PreparedStatement stmtExisteJuge;
    private static PreparedStatement stmtInsertJuge;
    private static PreparedStatement stmtExisteAvocat;
    private static PreparedStatement stmtInsertAvocat;
    private static PreparedStatement stmtExistePartie;
    private static PreparedStatement stmtInsertPartie;
    private static PreparedStatement stmtExisteProces;
    private static PreparedStatement stmtInsertProces;
    private static PreparedStatement stmtExisteJury;
    private static PreparedStatement stmtInsertJury;
    private static PreparedStatement stmtExisteJuryDansProces;
    private static PreparedStatement stmtInsertJuryDansProces;
    private static PreparedStatement stmtExisteSeance;
    private static PreparedStatement stmtInsertSeance;
    private static PreparedStatement stmtSelectJuges;
    private static PreparedStatement stmtSelectProces;
    private static PreparedStatement stmtSelectJurys;
    private static PreparedStatement stmtTerminerProces;
    private static PreparedStatement stmtJugeDisponible;
    private static PreparedStatement stmtJugeVerification;
    private static PreparedStatement stmtJugeRetirer;
    private static PreparedStatement stmtVerificationSeanceDecision;
    private static PreparedStatement stmtSupprimerSeance;
    private static PreparedStatement stmtExisteProcesSeances;

    /**
     * La fonction principale
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception
    {
        if (args.length < 4)
        {
            System.out.println("Usage: java tp2.Devoir2 <serveur> <bd> <user> <password> [<fichier-transactions>]");
            return;
        }

        cx = null;

        try
        {
            cx = new Connexion(args[0], args[1], args[2], args[3]);
            initialiseStatements();
            BufferedReader reader = ouvrirFichier(args);
            String transaction = lireTransaction(reader);
            while (!finTransaction(transaction))
            {
                executerTransaction(transaction);
                transaction = lireTransaction(reader);
            }
        }
        finally
        {
            if (cx != null)
                cx.fermer();
        }
    }

    /**
     * @throws SQLException
     * 
     */
    private static void initialiseStatements() throws SQLException
    {
        stmtVerificationSeanceDecision = cx.getConnection().prepareStatement("select * from \"Proces\" where \"id\" = ? and \"decision\" is null");
        stmtSelectProcesNonTermine = cx.getConnection()
                .prepareStatement("select * from \"Proces\" where \"id\" = ? and date < current_date");
        stmtExisteProcesDansSeance = cx.getConnection()
                .prepareStatement("select * from \"Seance\" where \"Proces_id\" = ?");
        stmtExisteJuge = cx.getConnection().prepareStatement("select * from \"Juge\" where id = ?");
        stmtInsertJuge = cx.getConnection()
                .prepareStatement("insert into \"Juge\" (id, prenom, nom, age) " + "values (?,?,?,?)");
        stmtExisteAvocat = cx.getConnection().prepareStatement("select * from \"Avocat\" where id = ?");
        stmtInsertAvocat = cx.getConnection()
                .prepareStatement("insert into \"Avocat\" (id, prenom, nom, type) " + "values (?,?,?,?)");
        stmtExistePartie = cx.getConnection().prepareStatement("select * from \"Partie\" where id = ?");
        stmtInsertPartie = cx.getConnection()
                .prepareStatement("insert into \"Partie\" (id, prenom, nom, \"Avocat_id\") " + "values (?,?,?,?)");
        stmtExisteProces = cx.getConnection().prepareStatement("select * from \"Proces\" where id = ?");
        stmtInsertProces = cx.getConnection().prepareStatement(
                "insert into \"Proces\" (id, \"Juge_id\", date, \"devantJury\", \"PartieDefenderesse_id\", \"PartiePoursuivant_id\") "
                        + "values (?,?,?,?,?,?)");
        stmtExisteJury = cx.getConnection().prepareStatement("select * from \"Jury\" where nas = ?");
        stmtInsertJury = cx.getConnection().prepareStatement(
                "insert into \"Jury\" (nas, prenom, nom, sexe, age, \"Proces_id\") " + "values (?,?,?,?,?,null)");
        stmtExisteJuryDansProces = cx.getConnection()
                .prepareStatement("select * from \"Jury\" where \"Proces_id\" = ?");
        stmtInsertJuryDansProces = cx.getConnection()
                .prepareStatement("update \"Jury\" set \"Proces_id\" = ? where \"nas\" = ?");
        stmtExisteSeance = cx.getConnection().prepareStatement("select * from \"Seance\" where id = ?");
        stmtInsertSeance = cx.getConnection()
                .prepareStatement("insert into \"Seance\" (id, date, \"Proces_id\") " + "values (?,?,?)");
        stmtSelectJuges = cx.getConnection().prepareStatement("select * from \"Juge\" where \"statutActif\" = true");
        stmtSelectProces = cx.getConnection().prepareStatement("select * from \"Proces\" where id = ?");
        stmtSelectJurys = cx.getConnection().prepareStatement("select * from \"Jury\" where \"Proces_id\" is null");
        stmtTerminerProces = cx.getConnection()
                .prepareStatement("update \"Proces\" set \"decision\" = ? where \"id\" = ?");
        stmtJugeDisponible = cx.getConnection().prepareStatement("select * from \"Proces\" where \"Juge_id\" = ?");
        stmtJugeVerification = cx.getConnection().prepareStatement("select * from \"Juge\" where \"id\" = ?");
        stmtJugeRetirer = cx.getConnection().prepareStatement("delete from \"Juge\" where \"id\" = ?");
        stmtSupprimerSeance = cx.getConnection().prepareStatement("delete from \"Seance\" where id = ?");
        cx.getConnection().prepareStatement("select * from \"Proces\" where id = ? and decision is null");
        stmtExisteProcesSeances = cx.getConnection()
                .prepareStatement("select * from \"Seance\" where \"Proces_id\" = ? and date > current_date");
    }

    /**
     * Decodage et traitement d'une transaction
     */
    static void executerTransaction(String transaction) throws Exception, IFT287Exception
    {
        try
        {
            System.out.print(transaction);
            // Decoupage de la transaction en mots
            StringTokenizer tokenizer = new StringTokenizer(transaction, " ");
            if (tokenizer.hasMoreTokens())
            {
                String command = tokenizer.nextToken();
                // Vous devez remplacer la chaine "commande1" et "commande2" par
                // les commandes de votre programme. Vous pouvez ajouter autant
                // de else if que necessaire. Vous n'avez pas a traiter la
                // commande "quitter".
                if (command.equals("ajouterJuge"))
                {
                    // Lecture des parametres
                    int idJuge = readInt(tokenizer);
                    String prenomJuge = readString(tokenizer);
                    String nomJuge = readString(tokenizer);
                    int ageJuge = readInt(tokenizer);

                    // Exemple a supprimer quand on rend le projet
                    // String param1 = readString(tokenizer);
                    // Date param2 = readDate(tokenizer);
                    // int param3 = readInt(tokenizer);

                    // Appel de la methode qui traite la transaction specifique
                    effectuerAjouterJuge(idJuge, prenomJuge, nomJuge, ageJuge);
                }
                else if (command.equals("retirerJuge"))
                {
                    // Lecture des parametres
                    int idJuge = readInt(tokenizer);

                    // Appel de la methode qui traite la transaction specifique
                    effectuerRetirerJuge(idJuge);
                }
                else if (command.equals("ajouterAvocat"))
                {
                    // Lecture des parametres
                    int idAvocat = readInt(tokenizer);
                    String prenomAvocat = readString(tokenizer);
                    String nomAvocat = readString(tokenizer);
                    int typeAvocat = readInt(tokenizer);

                    // Appel de la methode qui traite la transaction specifique
                    effectuerAjouterAvocat(idAvocat, prenomAvocat, nomAvocat, typeAvocat);
                }
                else if (command.equals("ajouterPartie"))
                {
                    // Lecture des parametres
                    int idPartie = readInt(tokenizer);
                    String prenomPartie = readString(tokenizer);
                    String nomPartie = readString(tokenizer);
                    int idAvocat = readInt(tokenizer);

                    // Appel de la methode qui traite la transaction specifique
                    effectuerAjouterPartie(idPartie, prenomPartie, nomPartie, idAvocat);
                }
                else if (command.equals("creerProces"))
                {
                    // Lecture des parametres
                    int idProces = readInt(tokenizer);
                    int idJuge = readInt(tokenizer);
                    Date dateInitiale = readDate(tokenizer);
                    int devantJury = readInt(tokenizer);
                    int idPartieDefenderesse = readInt(tokenizer);
                    int idPartiePoursuivante = readInt(tokenizer);

                    // Appel de la methode qui traite la transaction specifique
                    effectuerCreerProces(idProces, idJuge, dateInitiale, devantJury, idPartieDefenderesse,
                            idPartiePoursuivante);
                }
                else if (command.equals("inscrireJury"))
                {
                    // Lecture des parametres
                    int nasJury = readInt(tokenizer);
                    String prenomJury = readString(tokenizer);
                    String nomJury = readString(tokenizer);
                    String sexeJury = readString(tokenizer);
                    int ageJury = readInt(tokenizer);

                    // Appel de la methode qui traite la transaction specifique
                    effectuerInscrireJury(nasJury, prenomJury, nomJury, sexeJury, ageJury);
                }
                else if (command.equals("assignerJury"))
                {
                    // Lecture des parametres
                    int nasJury = readInt(tokenizer);
                    int idProces = readInt(tokenizer);

                    // Appel de la methode qui traite la transaction specifique
                    effectuerAssignerJury(nasJury, idProces);
                }
                else if (command.equals("ajouterSeance"))
                {
                    // Lecture des parametres
                    int idSeance = readInt(tokenizer);
                    int idProces = readInt(tokenizer);
                    Date dateSeance = readDate(tokenizer);

                    // Appel de la methode qui traite la transaction specifique
                    effectuerAjouterSeance(idSeance, idProces, dateSeance);
                }
                else if (command.equals("supprimerSeance"))
                {
                    // Lecture des parametres
                    int idSeance = readInt(tokenizer);

                    // Appel de la methode qui traite la transaction specifique
                    effectuerSupprimerSeance(idSeance);
                }
                else if (command.equals("terminerProces"))
                {
                    // Lecture des parametres
                    int idProces = readInt(tokenizer);
                    int decisionProces = readInt(tokenizer);

                    // Appel de la methode qui traite la transaction specifique
                    effectuerTerminerProces(idProces, decisionProces);
                }
                else if (command.equals("afficherJuges"))
                {
                    // Appel de la methode qui traite la transaction specifique
                    effectuerAfficherJuges();
                }
                else if (command.equals("afficherProces"))
                {
                    // Lecture des parametres
                    int idProces = readInt(tokenizer);

                    // Appel de la methode qui traite la transaction specifique
                    effectuerAfficherProces(idProces);
                }
                else if (command.equals("afficherJurys"))
                {
                    // Appel de la methode qui traite la transaction specifique
                    effectuerAfficherJurys();
                }
                else
                {
                    System.out.println(" : Transaction non reconnue");
                }
            }
        }
        catch (Exception e)
        {
            System.out.println(" " + e.toString());
            cx.rollback();
        }
    }

    /**
     * Methode d'affichage des jurys
     * 
     * @throws SQLException,
     *             IFT287Exception
     * 
     */
    private static void effectuerAfficherJurys() throws SQLException, IFT287Exception
    {
        try
        {
            ResultSet rsetJury = stmtSelectJurys.executeQuery();

            if (!rsetJury.next())
            {
                rsetJury.close();
                throw new IFT287Exception("Aucun jury n'est disponible !");
            }

            System.out.println("\n\nListe des jurys : ");

            do
            {
                System.out.println(rsetJury.getInt(1) + "\t" + rsetJury.getString(2) + "\t" + rsetJury.getString(3)
                        + "\t" + rsetJury.getString(4) + "\t" + rsetJury.getInt(5));
            }
            while (rsetJury.next());

            rsetJury.close();

            // Commit
            cx.commit();
        }
        catch (Exception e)
        {
            cx.rollback();
            throw e;
        }
    }

    /**
     * Methode d'affichage des proces
     * 
     * @param idProces
     * @throws SQLException,
     *             IFT287Exception
     */
    private static void effectuerAfficherProces(int idProces) throws SQLException, IFT287Exception
    {
        try
        {
            // Affichage du proces
            stmtSelectProces.setInt(1, idProces);
            ResultSet rsetProces = stmtSelectProces.executeQuery();

            if (!rsetProces.next())
            {
                rsetProces.close();
                throw new IFT287Exception("Le proces " + idProces + "n'existe pas");
            }

            System.out.println("\n\nAffichage du proces " + idProces + " :");

            System.out.println(rsetProces.getInt(1) + "\t" + rsetProces.getInt(2) + "\t" + rsetProces.getString(3)
                    + "\t" + rsetProces.getInt(4) + "\t" + rsetProces.getInt(5) + "\t" + rsetProces.getInt(6));

            rsetProces.close();

            // Affichage des seance liees au proces
            stmtExisteProcesDansSeance.setInt(1, idProces);
            rsetProces = stmtExisteProcesDansSeance.executeQuery();

            if (!rsetProces.next())
            {
                rsetProces.close();
                throw new IFT287Exception("Aucune seance n'est liee au proces " + idProces);
            }

            System.out.println("\nListe des seances liees au proces " + idProces + " :");

            do
            {
                System.out.println(rsetProces.getInt(1) + "\t" + rsetProces.getString(2) + "\t" + rsetProces.getInt(3));
            }
            while (rsetProces.next());

            rsetProces.close();

            // Commit
            cx.commit();
        }
        catch (Exception e)
        {
            cx.rollback();
            throw e;
        }
    }

    /**
     * Methode d'affichage des juges
     * 
     * @throws SQLException,
     *             IFT287Exception
     * 
     */
    private static void effectuerAfficherJuges() throws SQLException, IFT287Exception
    {
        try
        {
            ResultSet rsetJuges = stmtSelectJuges.executeQuery();

            if (rsetJuges.next())
            {
                rsetJuges.close();
                throw new IFT287Exception("Aucun juge actif ni disponible !");
            }
            rsetJuges.close();

            System.out.println("\nListe des juges actifs et disponibles :");

            do
            {
                System.out.println(rsetJuges.getInt(1) + "\t" + rsetJuges.getString(2) + "\t" + rsetJuges.getString(3)
                        + "\t" + rsetJuges.getInt(4));
            }
            while (rsetJuges.next());

            // Commit
            cx.commit();
        }
        catch (Exception e)
        {
            cx.rollback();
            throw e;
        }
    }

    /**
     * Methode de traitement pour effectuerTerminerProces
     * 
     * @param idProces
     * @param decisionProces
     * @throws SQLException,
     *             IFT287Exception
     */
    private static void effectuerTerminerProces(int idProces, int decisionProces) throws SQLException, IFT287Exception
    {
        try
        {
            // Vérification que le proces existe
            stmtExisteProces.setInt(1, idProces);
            ResultSet rsetTermineProces = stmtExisteProces.executeQuery();

            if (!rsetTermineProces.next())
            {
                rsetTermineProces.close();
                throw new IFT287Exception("Le proces " + idProces + "n'existe pas");
            }
            rsetTermineProces.close();

            // Vérification que le proces a atteint sa date initiale
            stmtSelectProcesNonTermine.setInt(1, idProces);
            rsetTermineProces = stmtSelectProcesNonTermine.executeQuery();

            if (!rsetTermineProces.next())
            {
                rsetTermineProces.close();
                throw new IFT287Exception("Le proces " + idProces + "n'a pas atteint sa date initiale");
            }

            rsetTermineProces.close();

            // Terminer le proces
            stmtTerminerProces.setInt(1, decisionProces);
            stmtTerminerProces.setInt(2, idProces);
            stmtTerminerProces.executeUpdate();

            rsetTermineProces.close();

            // Suppresion des seances prevues du proces
            stmtExisteProcesSeances.setInt(1, idProces);
            rsetTermineProces = stmtExisteProcesSeances.executeQuery();

            while (rsetTermineProces.next())
            {
                effectuerSupprimerSeance(rsetTermineProces.getInt(1));
            }

            rsetTermineProces.close();

            // Commit
            cx.commit();
        }
        catch (Exception e)
        {
            cx.rollback();
            throw e;
        }
    }

    /**
     * Methode de traitement pour effectuerSupprimerSeance
     * 
     * @param idSeance
     * @throws SQLException,
     *             IFT287Exception
     */
    private static void effectuerSupprimerSeance(int idSeance) throws SQLException, IFT287Exception
    {
        try
        {
            stmtExisteSeance.setInt(1, idSeance);
            stmtExisteSeance.setInt(1, idSeance);
            ResultSet rsetSeanceExiste = stmtExisteSeance.executeQuery();
            ResultSet rsetSeanceDate;

            // Si la seance existe
            if (!rsetSeanceExiste.next())
            {
                rsetSeanceExiste.close();
                throw new IFT287Exception("La seance: " + idSeance + " n'existe pas!");
            }

            rsetSeanceExiste.close();

            rsetSeanceDate = stmtExisteSeance.executeQuery();

            // Si la date est déjà passée ou non
            if (!rsetSeanceDate.next())
            {
                rsetSeanceDate.close();
                throw new IFT287Exception("La seance: " + idSeance + " est deja passe!");

            }

            rsetSeanceDate.close();

            // Suppression de la seance
            stmtSupprimerSeance.setInt(1, idSeance);
            stmtSupprimerSeance.executeUpdate();

            // Commit
            cx.commit();
        }
        catch (Exception e)
        {
            cx.rollback();
            throw e;
        }
    }

    /**
     * Methode de traitement pour effectuerAjouterSeance
     * 
     * @param idSeance
     * @param idProces
     * @param dateSeance
     * @throws SQLException,
     *             IFT287Exception
     */
    private static void effectuerAjouterSeance(int idSeance, int idProces, Date dateSeance)
            throws SQLException, IFT287Exception
    {
        try
        {
            // Vérification si la seance existe deja
            stmtExisteSeance.setInt(1, idSeance);
            ResultSet rsetSeance = stmtExisteSeance.executeQuery();

            if (rsetSeance.next())
            {
                rsetSeance.close();
                throw new IFT287Exception("La seance existe deja: " + idSeance);
            }
            rsetSeance.close();

            // Verification si le proces specifie n'est pas termine
            stmtVerificationSeanceDecision.setInt(1, idProces);
            rsetSeance = stmtVerificationSeanceDecision.executeQuery();

            if (!rsetSeance.next())
            {
                rsetSeance.close();
                throw new IFT287Exception("Le proces " + idProces + " est termine.");
            }
            rsetSeance.close();

            // Ajout de la seance
            stmtInsertSeance.setInt(1, idSeance);
            stmtInsertSeance.setDate(2, dateSeance);
            stmtInsertSeance.setInt(3, idProces);
            stmtInsertSeance.executeUpdate();

            // Commit
            cx.commit();
        }
        catch (Exception e)
        {
            cx.rollback();
            throw e;
        }
    }

    /**
     * Methode de traitement pour effectuerAssignerJury
     * 
     * @param nasJury
     * @param idProces
     * @throws SQLException,
     *             IFT287Exception
     */
    private static void effectuerAssignerJury(int nasJury, int idProces) throws SQLException, IFT287Exception
    {
        try
        {
            stmtExisteJuryDansProces.setInt(1, idProces);
            ResultSet rsetJuryDansProces = stmtExisteJuryDansProces.executeQuery();

            if (rsetJuryDansProces.next())
            {
                rsetJuryDansProces.close();
                throw new IFT287Exception("Le proces " + idProces + " est deja occupe par un jury");
            }
            rsetJuryDansProces.close();

            // Ajout du jury
            stmtInsertJuryDansProces.setInt(1, idProces);
            stmtInsertJuryDansProces.setInt(2, nasJury);
            stmtInsertJuryDansProces.executeUpdate();

            // Commit
            cx.commit();
        }
        catch (Exception e)
        {
            cx.rollback();
            throw e;
        }
    }

    /**
     * Methode de traitement pour effectuerInscrireJury
     * 
     * @param nasJury
     * @param prenomJury
     * @param nomJury
     * @param sexeJury
     * @param ageJury
     * @throws SQLException,
     *             IFT287Exception
     */
    private static void effectuerInscrireJury(int nasJury, String prenomJury, String nomJury, String sexeJury,
            int ageJury) throws SQLException, IFT287Exception
    {
        try
        {
            stmtExisteJury.setInt(1, nasJury);
            ResultSet rsetJury = stmtExisteJury.executeQuery();

            if (rsetJury.next())
            {
                rsetJury.close();
                throw new IFT287Exception("Jury existe deja: " + nasJury);
            }
            rsetJury.close();

            // Ajout du jury
            stmtInsertJury.setInt(1, nasJury);
            stmtInsertJury.setString(2, prenomJury);
            stmtInsertJury.setString(3, nomJury);
            stmtInsertJury.setString(4, sexeJury);
            stmtInsertJury.setInt(5, ageJury);
            stmtInsertJury.executeUpdate();

            // Commit
            cx.commit();
        }
        catch (Exception e)
        {
            cx.rollback();
            throw e;
        }
    }

    /**
     * Methode de traitement pour effectuerCreerProces
     * 
     * @param idProces
     * @param idJuge
     * @param dateInitiale
     * @param devantJury
     * @param idPartieDefenderesse
     * @param idPartiePoursuivante
     * @throws SQLException,
     *             IFT287Exception
     */
    private static void effectuerCreerProces(int idProces, int idJuge, Date dateInitiale, int devantJury,
            int idPartieDefenderesse, int idPartiePoursuivante) throws SQLException, IFT287Exception
    {
        try
        {
            stmtExisteProces.setInt(1, idProces);
            ResultSet rsetProces = stmtExisteProces.executeQuery();

            if (rsetProces.next())
            {
                rsetProces.close();
                throw new IFT287Exception("Proces existe deja: " + idProces);
            }
            rsetProces.close();

            // Ajout du proces
            stmtInsertProces.setInt(1, idProces);
            stmtInsertProces.setInt(2, idJuge);
            stmtInsertProces.setDate(3, dateInitiale);
            stmtInsertProces.setInt(4, devantJury);
            stmtInsertProces.setInt(5, idPartieDefenderesse);
            stmtInsertProces.setInt(6, idPartiePoursuivante);
            stmtInsertProces.executeUpdate();

            // Commit
            cx.commit();
        }
        catch (Exception e)
        {
            cx.rollback();
            throw e;
        }
    }

    /**
     * Methode de traitement pour effectuerAjouterPartie
     * 
     * @param idPartie
     * @param prenomPartie
     * @param nomPartie
     * @param idAvocat
     * @throws SQLException,
     *             IFT287Exception
     */
    private static void effectuerAjouterPartie(int idPartie, String prenomPartie, String nomPartie, int idAvocat)
            throws SQLException, IFT287Exception
    {
        try
        {
            stmtExistePartie.setInt(1, idAvocat);
            ResultSet rsetPartie = stmtExistePartie.executeQuery();

            if (rsetPartie.next())
            {
                rsetPartie.close();
                throw new IFT287Exception("Partie existe deja: " + idPartie);
            }
            rsetPartie.close();

            // Ajout du partie
            stmtInsertPartie.setInt(1, idPartie);
            stmtInsertPartie.setString(2, prenomPartie);
            stmtInsertPartie.setString(3, nomPartie);
            stmtInsertPartie.setInt(4, idAvocat);
            stmtInsertPartie.executeUpdate();

            // Commit
            cx.commit();
        }
        catch (Exception e)
        {
            cx.rollback();
            throw e;
        }
    }

    /**
     * Methode de traitement pour effectuerAjouterAvocat
     * 
     * @param idAvocat
     * @param prenomAvocat
     * @param nomAvocat
     * @param typeAvocat
     * @throws SQLException,
     *             IFT287Exception
     */
    private static void effectuerAjouterAvocat(int idAvocat, String prenomAvocat, String nomAvocat, int typeAvocat)
            throws SQLException, IFT287Exception
    {
        try
        {
            stmtExisteAvocat.setInt(1, idAvocat);
            ResultSet rsetAvocat = stmtExisteAvocat.executeQuery();

            if (rsetAvocat.next())
            {
                rsetAvocat.close();
                throw new IFT287Exception("Avocat existe deja: " + idAvocat);
            }
            rsetAvocat.close();

            // Ajout de l'avocat
            stmtInsertAvocat.setInt(1, idAvocat);
            stmtInsertAvocat.setString(2, prenomAvocat);
            stmtInsertAvocat.setString(3, nomAvocat);
            stmtInsertAvocat.setInt(4, typeAvocat);
            stmtInsertAvocat.executeUpdate();

            // Commit
            cx.commit();
        }
        catch (Exception e)
        {
            cx.rollback();
            throw e;
        }
    }

    /**
     * Methode de traitement pour effectuerRetirerJuge
     * 
     * @param idJuge
     * @throws SQLException,
     *             IFT287Exception
     */
    private static void effectuerRetirerJuge(int idJuge) throws SQLException, IFT287Exception
    {
        try
        {
            stmtJugeDisponible.setInt(1, idJuge);
            ResultSet rsetJugeDisponible = stmtJugeDisponible.executeQuery();

            if (rsetJugeDisponible.next())
            {
                rsetJugeDisponible.close();
                throw new IFT287Exception("Juge non disponible et/ou déjà inactif: " + idJuge);
            }

            rsetJugeDisponible.close();

            stmtJugeVerification.setInt(1, idJuge);

            ResultSet rsetJugeVerification = stmtJugeVerification.executeQuery();

            if (!(rsetJugeVerification.next()))
            {
                rsetJugeVerification.close();
                throw new IFT287Exception("Juge inexistant: " + idJuge);
            }
            rsetJugeVerification.close();

            // Ajout de l'avocat
            stmtJugeRetirer.setInt(1, idJuge);
            stmtJugeRetirer.executeUpdate();

            // Commit
            cx.commit();
        }
        catch (Exception e)
        {
            cx.rollback();
            throw e;
        }
    }

    /**
     * Methode de traitement pour effectuerAjouterJuge
     * 
     * @param idJuge
     * @param prenomJuge
     * @param nomJuge
     * @param ageJuge
     * @throws SQLException,
     *             IFT287Exception
     */
    private static void effectuerAjouterJuge(int idJuge, String prenomJuge, String nomJuge, int ageJuge)
            throws SQLException, IFT287Exception
    {
        try
        {
            stmtExisteJuge.setInt(1, idJuge);
            ResultSet rsetJuge = stmtExisteJuge.executeQuery();

            if (rsetJuge.next())
            {
                rsetJuge.close();
                throw new IFT287Exception("Juge existe deja: " + idJuge);
            }
            rsetJuge.close();

            // Ajout du juge
            stmtInsertJuge.setInt(1, idJuge);
            stmtInsertJuge.setString(2, prenomJuge);
            stmtInsertJuge.setString(3, nomJuge);
            stmtInsertJuge.setInt(4, ageJuge);
            stmtInsertJuge.executeUpdate();

            // Commit
            cx.commit();
        }
        catch (Exception e)
        {
            cx.rollback();
            throw e;
        }
    }

    // ****************************************************************
    // * Les methodes suivantes n'ont pas besoin d'etre modifiees *
    // ****************************************************************

    /**
     * Ouvre le fichier de transaction, ou lit à partir de System.in
     * 
     * @param args
     * @return BufferedReader
     * @throws FileNotFoundException
     */
    public static BufferedReader ouvrirFichier(String[] args) throws FileNotFoundException
    {
        if (args.length < 5)
            // Lecture au clavier
            return new BufferedReader(new InputStreamReader(System.in));
        else
            // Lecture dans le fichier passe en parametre
            return new BufferedReader(new InputStreamReader(new FileInputStream(args[4])));
    }

    /**
     * Lecture d'une transaction
     */
    static String lireTransaction(BufferedReader reader) throws IOException
    {
        return reader.readLine();
    }

    /**
     * Verifie si la fin du traitement des transactions est atteinte.
     */
    static boolean finTransaction(String transaction)
    {
        // fin de fichier atteinte
        return (transaction == null || transaction.equals("quitter"));
    }

    /** Lecture d'une chaine de caracteres de la transaction entree a l'ecran */
    static String readString(StringTokenizer tokenizer) throws Exception
    {
        if (tokenizer.hasMoreElements())
            return tokenizer.nextToken();
        else
            throw new Exception("Autre parametre attendu");
    }

    /**
     * Lecture d'un int java de la transaction entree a l'ecran
     */
    static int readInt(StringTokenizer tokenizer) throws Exception
    {
        if (tokenizer.hasMoreElements())
        {
            String token = tokenizer.nextToken();
            try
            {
                return Integer.valueOf(token).intValue();
            }
            catch (NumberFormatException e)
            {
                throw new Exception("Nombre attendu a la place de \"" + token + "\"");
            }
        }
        else
            throw new Exception("Autre parametre attendu");
    }

    static Date readDate(StringTokenizer tokenizer) throws Exception
    {
        if (tokenizer.hasMoreElements())
        {
            String token = tokenizer.nextToken();
            try
            {
                return Date.valueOf(token);
            }
            catch (IllegalArgumentException e)
            {
                throw new Exception("Date dans un format invalide - \"" + token + "\"");
            }
        }
        else
            throw new Exception("Autre parametre attendu");
    }

}