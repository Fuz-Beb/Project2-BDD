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

    // Proces
    private static PreparedStatement stmtExisteProces;
    private static PreparedStatement stmtInsertProces;
    private static PreparedStatement stmtSelectProcesNonTermine;
    private static PreparedStatement stmtTerminerProces;
    private static PreparedStatement stmtVerificationProcesDecision;
    private static PreparedStatement stmtProcesJugeEnCours;
    private static PreparedStatement stmtVerificationProcesDevantJury;

    // Seance
    private static PreparedStatement stmtExisteSeance;
    private static PreparedStatement stmtInsertSeance;
    private static PreparedStatement stmtExisteProcesDansSeance;
    private static PreparedStatement stmtSupprimerSeancesProcesTermine;
    private static PreparedStatement stmtSeanceNonTerminee;
    private static PreparedStatement stmtSupprimerSeance;

    // Jury
    private static PreparedStatement stmtExisteJury;
    private static PreparedStatement stmtInsertJury;
    private static PreparedStatement stmtSelectJurys;
    private static PreparedStatement stmtInsertJuryDansProces;

    // Juge
    private static PreparedStatement stmtExisteJuge;
    private static PreparedStatement stmtInsertJuge;
    private static PreparedStatement stmtSelectJuges;
    private static PreparedStatement stmtRetirerJuge;

    // Partie
    private static PreparedStatement stmtExistePartie;
    private static PreparedStatement stmtInsertPartie;

    // Avocat
    private static PreparedStatement stmtExisteAvocat;
    private static PreparedStatement stmtInsertAvocat;

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
        // Proces
        stmtExisteProces = cx.getConnection().prepareStatement("select * from \"Proces\" where \"id\" = ?");
        stmtSelectProcesNonTermine = cx.getConnection()
                .prepareStatement("select * from \"Proces\" where \"id\" = ? and \"date\" < current_date");
        stmtTerminerProces = cx.getConnection()
                .prepareStatement("update \"Proces\" set \"decision\" = ? where \"id\" = ?");
        stmtVerificationProcesDecision = cx.getConnection()
                .prepareStatement("select * from \"Proces\" where \"id\" = ? and \"decision\" is null");
        stmtInsertProces = cx.getConnection().prepareStatement(
                "insert into \"Proces\" (\"id\", \"Juge_id\", \"date\", \"devantJury\", \"PartieDefenderesse_id\", \"PartiePoursuivant_id\") "
                        + "values (?,?,?,?,?,?)");
        stmtProcesJugeEnCours = cx.getConnection()
                .prepareStatement("select * from \"Proces\" where \"Juge_id\" = ? and \"decision\" is null");
        stmtVerificationProcesDevantJury = cx.getConnection()
                .prepareStatement("select from \"Proces\" where \"id\" = ? and \"devantJury\" = 1");

        // Seance
        stmtExisteSeance = cx.getConnection().prepareStatement("select * from \"Seance\" where id = ?");
        stmtExisteProcesDansSeance = cx.getConnection()
                .prepareStatement("select * from \"Seance\" where \"Proces_id\" = ?");
        stmtSupprimerSeancesProcesTermine = cx.getConnection()
                .prepareStatement("select * from \"Seance\" where \"Proces_id\" = ? and date > current_date");
        stmtSeanceNonTerminee = cx.getConnection()
                .prepareStatement("select * from \"Seance\" where \"id\" = ? and \"date\" < current_date");
        stmtSupprimerSeance = cx.getConnection().prepareStatement("delete from \"Seance\" where \"id\" = ?");
        stmtInsertSeance = cx.getConnection()
                .prepareStatement("insert into \"Seance\" (\"id\", \"date\", \"Proces_id\") " + "values (?,?,?)");

        // Jury
        stmtSelectJurys = cx.getConnection().prepareStatement("select * from \"Jury\" where \"Proces_id\" is null");
        stmtInsertJuryDansProces = cx.getConnection()
                .prepareStatement("update \"Jury\" set \"Proces_id\" = ? where \"nas\" = ?");
        stmtExisteJury = cx.getConnection().prepareStatement("select * from \"Jury\" where \"nas\" = ?");
        stmtInsertJury = cx.getConnection().prepareStatement(
                "insert into \"Jury\" (\"nas\", \"prenom\", \"nom\", \"sexe\", \"age\", \"Proces_id\") "
                        + "values (?,?,?,?,?,null)");

        // Juge
        stmtSelectJuges = cx.getConnection().prepareStatement("select * from \"Juge\" where \"statutActif\" = false");
        stmtExisteJuge = cx.getConnection().prepareStatement("select * from \"Juge\" where \"id\" = ?");
        stmtInsertJuge = cx.getConnection()
                .prepareStatement("insert into \"Juge\" (\"id\", \"prenom\", \"nom\", \"age\") " + "values (?,?,?,?)");
        stmtRetirerJuge = cx.getConnection()
                .prepareStatement("update \"Juge\" set \"statutActif\" =  false where \"id\" = ?");

        // Partie
        stmtExistePartie = cx.getConnection().prepareStatement("select * from \"Partie\" where \"id\" = ?");
        stmtInsertPartie = cx.getConnection().prepareStatement(
                "insert into \"Partie\" (\"id\", \"prenom\", \"nom\", \"Avocat_id\") " + "values (?,?,?,?)");

        // Avocat
        stmtExisteAvocat = cx.getConnection().prepareStatement("select * from \"Avocat\" where \"id\" = ?");
        stmtInsertAvocat = cx.getConnection()
                .prepareStatement("insert into \"Avocat\" (id, prenom, nom, type) " + "values (?,?,?,?)");
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
            // Récupération de la liste des jurys
            ResultSet rsetJury = stmtSelectJurys.executeQuery();

            if (!rsetJury.next())
            {
                rsetJury.close();
                throw new IFT287Exception("Aucun jury n'est disponible !");
            }

            System.out.println("\n\nListe des jurys : ");

            // Affichage de la liste des jurys
            do
            {
                System.out.println(rsetJury.getInt(1) + "\t" + rsetJury.getString(2) + "\t" + rsetJury.getString(3)
                        + "\t" + rsetJury.getString(4) + "\t" + rsetJury.getInt(5));
            }
            while (rsetJury.next());

            rsetJury.close();

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
            // Verification du numero du proces
            stmtExisteProces.setInt(1, idProces);
            ResultSet rsetProces = stmtExisteProces.executeQuery();

            if (!rsetProces.next())
            {
                rsetProces.close();
                throw new IFT287Exception("Le proces " + idProces + "n'existe pas");
            }

            System.out.println("\n\nAffichage du proces " + idProces + " :");

            // Affichage des elements du proces conernes
            System.out.println(rsetProces.getInt(1) + "\t" + rsetProces.getInt(2) + "\t" + rsetProces.getString(3)
                    + "\t" + rsetProces.getInt(4) + "\t" + rsetProces.getInt(5) + "\t" + rsetProces.getInt(6));

            rsetProces.close();

            // Récupération des seances liees au proces
            stmtExisteProcesDansSeance.setInt(1, idProces);
            rsetProces = stmtExisteProcesDansSeance.executeQuery();

            if (rsetProces.next())
            {
                System.out.println("\nListe des seances liees au proces " + idProces + " :");

                // Affichage des seances liees au proces
                do
                {
                    System.out.println(
                            rsetProces.getInt(1) + "\t" + rsetProces.getString(2) + "\t" + rsetProces.getInt(3));
                }
                while (rsetProces.next());

                rsetProces.close();
            }
            else
            {
                System.out.println("Aucune seance n'est liee au proces " + idProces);
                rsetProces.close();
            }
                

            cx.commit();
        }
        catch (Exception e)
        {
            cx.rollback();
            throw e;
        }
    }

    /**
     * Methode d'affichage des juges actifs et disponibles
     * 
     * @throws SQLException,
     *             IFT287Exception
     * 
     */
    private static void effectuerAfficherJuges() throws SQLException, IFT287Exception
    {
        try
        {
            // Récupération de la liste des juges actifs et disponibles
            ResultSet rsetJuges = stmtSelectJuges.executeQuery();

            if (!rsetJuges.next())
            {
                rsetJuges.close();
                throw new IFT287Exception("Aucun juge n'est actuellement actif et disponible.");
            }

            System.out.println("\nListe des juges actifs et disponibles :");

            // Affichage de la liste des juges actifs et disponibles
            do
            {
                System.out.println(rsetJuges.getInt(1) + "\t" + rsetJuges.getString(2) + "\t" + rsetJuges.getString(3)
                        + "\t" + rsetJuges.getInt(4));
            }
            while (rsetJuges.next());

            rsetJuges.close();

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
            // Verification de la valeur de la decision
            if (decisionProces != 0 && decisionProces != 1)
                throw new IFT287Exception("Impossible de terminer le proces " + idProces
                        + "car la valeur de la decision n'est ni 0 ni 1.");

            // Vérification que le proces existe
            stmtExisteProces.setInt(1, idProces);
            ResultSet rsetTermineProces = stmtExisteProces.executeQuery();

            if (!rsetTermineProces.next())
            {
                rsetTermineProces.close();
                throw new IFT287Exception("Le proces " + idProces + "n'existe pas.");
            }
            rsetTermineProces.close();

            // Vérification que le proces a atteint sa date initiale
            stmtSelectProcesNonTermine.setInt(1, idProces);
            rsetTermineProces = stmtSelectProcesNonTermine.executeQuery();

            if (!rsetTermineProces.next())
            {
                rsetTermineProces.close();
                throw new IFT287Exception("Le proces " + idProces + "n'a pas atteint sa date initiale.");
            }

            rsetTermineProces.close();

            // Terminer le proces
            stmtTerminerProces.setInt(1, decisionProces);
            stmtTerminerProces.setInt(2, idProces);
            stmtTerminerProces.executeUpdate();

            // Suppresion des seances prevues du proces
            stmtSupprimerSeancesProcesTermine.setInt(1, idProces);
            rsetTermineProces = stmtSupprimerSeancesProcesTermine.executeQuery();

            // Suppression des seances une a une
            while (rsetTermineProces.next())
            {
                effectuerSupprimerSeance(rsetTermineProces.getInt(1));
            }

            rsetTermineProces.close();

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
            // Vérification de l'id de la seance
            stmtExisteSeance.setInt(1, idSeance);
            ResultSet rsetSupprimerSeance = stmtExisteSeance.executeQuery();

            if (!rsetSupprimerSeance.next())
            {
                rsetSupprimerSeance.close();
                throw new IFT287Exception("La seance: " + idSeance + " n'existe pas.");
            }

            rsetSupprimerSeance.close();

            // Vérification que la seance n'est pas encore passee
            stmtSeanceNonTerminee.setInt(1, idSeance);
            rsetSupprimerSeance = stmtSeanceNonTerminee.executeQuery();

            if (rsetSupprimerSeance.next())
            {
                rsetSupprimerSeance.close();
                throw new IFT287Exception("La seance: " + idSeance + " est deja passe.");
            }

            rsetSupprimerSeance.close();

            // Suppression de la seance
            stmtSupprimerSeance.setInt(1, idSeance);
            stmtSupprimerSeance.executeUpdate();

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
            ResultSet rsetAjouterSeance = stmtExisteSeance.executeQuery();

            if (rsetAjouterSeance.next())
            {
                rsetAjouterSeance.close();
                throw new IFT287Exception("La seance existe deja: " + idSeance);
            }
            rsetAjouterSeance.close();

            // Verification si le proces existe
            stmtExisteProces.setInt(1, idProces);
            rsetAjouterSeance = stmtExisteProces.executeQuery();

            if (!rsetAjouterSeance.next())
            {
                rsetAjouterSeance.close();
                throw new IFT287Exception("Le proces " + idProces + " n'existe pas.");
            }
            rsetAjouterSeance.close();

            // Verification si le proces specifie n'est pas termine
            stmtVerificationProcesDecision.setInt(1, idProces);
            rsetAjouterSeance = stmtVerificationProcesDecision.executeQuery();

            if (!rsetAjouterSeance.next())
            {
                rsetAjouterSeance.close();
                throw new IFT287Exception("Le proces " + idProces + " est termine.");
            }
            rsetAjouterSeance.close();

            // Ajout de la seance
            stmtInsertSeance.setInt(1, idSeance);
            stmtInsertSeance.setDate(2, dateSeance);
            stmtInsertSeance.setInt(3, idProces);
            stmtInsertSeance.executeUpdate();

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
            // Verification du nas du jury
            stmtExisteJury.setInt(1, nasJury);
            ResultSet rsetAssignerJury = stmtExisteJury.executeQuery();

            if (!rsetAssignerJury.next())
            {
                rsetAssignerJury.close();
                throw new IFT287Exception("Le jury " + nasJury + " n'existe pas.");
            }
            rsetAssignerJury.close();

            // Verification de l'id du proces
            stmtExisteProces.setInt(1, idProces);
            rsetAssignerJury = stmtExisteProces.executeQuery();

            if (!rsetAssignerJury.next())
            {
                rsetAssignerJury.close();
                throw new IFT287Exception("Le proces " + idProces + " n'existe pas.");
            }
            rsetAssignerJury.close();

            // Verification que le proces doit se tenir devant un jury
            // "devantJury = 1"
            stmtVerificationProcesDevantJury.setInt(1, idProces);
            rsetAssignerJury = stmtVerificationProcesDevantJury.executeQuery();

            if (!rsetAssignerJury.next())
            {
                rsetAssignerJury.close();
                throw new IFT287Exception("Le proces " + idProces + " doit se tenir devant un juge seul");
            }
            rsetAssignerJury.close();

            // Ajout du jury au proces concerne
            stmtInsertJuryDansProces.setInt(1, idProces);
            stmtInsertJuryDansProces.setInt(2, nasJury);
            stmtInsertJuryDansProces.executeUpdate();

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
            // Verification que le nas du jury n'existe pas deja
            stmtExisteJury.setInt(1, nasJury);
            ResultSet rsetInscrireJury = stmtExisteJury.executeQuery();

            if (rsetInscrireJury.next())
            {
                rsetInscrireJury.close();
                throw new IFT287Exception("Jury existe deja: " + nasJury);
            }
            rsetInscrireJury.close();

            // Ajout du jury
            stmtInsertJury.setInt(1, nasJury);
            stmtInsertJury.setString(2, prenomJury);
            stmtInsertJury.setString(3, nomJury);
            stmtInsertJury.setString(4, sexeJury);
            stmtInsertJury.setInt(5, ageJury);
            stmtInsertJury.executeUpdate();

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
            if (devantJury != 0 && devantJury != 1)
                throw new IFT287Exception("Impossible de creer le proces " + idProces
                        + "car le champ devantJury ne peut être que 0 ou 1");

            // Verification que le proces n'existe pas deja
            stmtExisteProces.setInt(1, idProces);
            ResultSet rsetCreerProces = stmtExisteProces.executeQuery();

            if (rsetCreerProces.next())
            {
                rsetCreerProces.close();
                throw new IFT287Exception("Proces existe deja: " + idProces);
            }
            rsetCreerProces.close();

            // Vérification que l'id du juge est correcte
            stmtExisteJuge.setInt(1, idJuge);
            rsetCreerProces = stmtExisteJuge.executeQuery();

            if (!rsetCreerProces.next())
            {
                rsetCreerProces.close();
                throw new IFT287Exception("Le juge " + idJuge + "n'existe pas.");
            }
            rsetCreerProces.close();

            // Verification que l'id des parties sont correctes -
            // PartieDefenderesse
            stmtExistePartie.setInt(1, idPartieDefenderesse);
            rsetCreerProces = stmtExisteJuge.executeQuery();

            if (!rsetCreerProces.next())
            {
                rsetCreerProces.close();
                throw new IFT287Exception("La partie defenderesse " + idPartieDefenderesse + "n'existe pas.");
            }
            rsetCreerProces.close();

            // Verification que l'id des parties sont correctes -
            // PartiePoursuivante
            stmtExistePartie.setInt(1, idPartiePoursuivante);
            rsetCreerProces = stmtExisteJuge.executeQuery();

            if (!rsetCreerProces.next())
            {
                rsetCreerProces.close();
                throw new IFT287Exception("La partie poursuivante " + idPartiePoursuivante + "n'existe pas.");
            }
            rsetCreerProces.close();

            // Ajout du proces
            stmtInsertProces.setInt(1, idProces);
            stmtInsertProces.setInt(2, idJuge);
            stmtInsertProces.setDate(3, dateInitiale);
            stmtInsertProces.setInt(4, devantJury);
            stmtInsertProces.setInt(5, idPartieDefenderesse);
            stmtInsertProces.setInt(6, idPartiePoursuivante);
            stmtInsertProces.executeUpdate();

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
            // Verification que la partie mentionnee n'existe pas deja.
            stmtExistePartie.setInt(1, idAvocat);
            ResultSet rsetAjouterPartie = stmtExistePartie.executeQuery();

            if (rsetAjouterPartie.next())
            {
                rsetAjouterPartie.close();
                throw new IFT287Exception("Partie existe deja: " + idPartie);
            }
            rsetAjouterPartie.close();

            // Verification que l'id de l'avocat est correcte
            stmtExisteAvocat.setInt(1, idAvocat);
            rsetAjouterPartie = stmtExisteAvocat.executeQuery();

            if (!rsetAjouterPartie.next())
            {
                rsetAjouterPartie.close();
                throw new IFT287Exception("L'avocat " + idAvocat + "n'existe pas.");
            }
            rsetAjouterPartie.close();

            // Ajout du partie
            stmtInsertPartie.setInt(1, idPartie);
            stmtInsertPartie.setString(2, prenomPartie);
            stmtInsertPartie.setString(3, nomPartie);
            stmtInsertPartie.setInt(4, idAvocat);
            stmtInsertPartie.executeUpdate();

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
            // Verification que l'id avocat n'existe pas deja
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
            // Verification que l'id du juge est correcte
            stmtExisteJuge.setInt(1, idJuge);
            ResultSet rsetRetirerJuge = stmtExisteJuge.executeQuery();

            if (!(rsetRetirerJuge.next()))
            {
                rsetRetirerJuge.close();
                throw new IFT287Exception("Juge inexistant: " + idJuge);
            }
            rsetRetirerJuge.close();

            // Verification que le juge concerne n'a pas encore de proces qui
            // est non termine
            stmtProcesJugeEnCours.setInt(1, idJuge);
            rsetRetirerJuge = stmtProcesJugeEnCours.executeQuery();

            if (rsetRetirerJuge.next())
            {
                rsetRetirerJuge.close();
                throw new IFT287Exception("Le juge " + idJuge + "n'a pas termine tous ses proces");
            }

            rsetRetirerJuge.close();

            // Retire le juge concerne de la liste des juges actifs et
            // disponibles
            stmtRetirerJuge.setInt(1, idJuge);
            stmtRetirerJuge.executeUpdate();

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
            // Verification que le juge n'existe pas deja
            stmtExisteJuge.setInt(1, idJuge);
            ResultSet rsetAjouterJuge = stmtExisteJuge.executeQuery();

            if (rsetAjouterJuge.next())
            {
                rsetAjouterJuge.close();
                throw new IFT287Exception("Juge existe deja: " + idJuge);
            }
            rsetAjouterJuge.close();

            // Ajout du juge
            stmtInsertJuge.setInt(1, idJuge);
            stmtInsertJuge.setString(2, prenomJuge);
            stmtInsertJuge.setString(3, nomJuge);
            stmtInsertJuge.setInt(4, ageJuge);
            stmtInsertJuge.executeUpdate();

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