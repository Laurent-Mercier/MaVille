package ca.umontreal.user;

public class User {
    private String userType;
    private String nom;
    private String courriel;
    private String password;
    private String dateNaissance;
    private String telephone;
    private String adresse;
    private String arrondissement;
    private String typeIntervenant;
    private String idVille;
    private String horaire;

    public User(String userType) {
        this.userType=userType;
        this.nom="";
        this.courriel="";
        this.password="";
        this.dateNaissance="";
        this.telephone="";
        this.adresse="";
        this.typeIntervenant="";
        this.idVille="";
        this.horaire="";
    }

    public String getUserType() {
        return this.userType;
    }
    public String getNom() {
        return this.nom;
    }

    public String getCourriel() {
        return this.courriel;
    }

    public String getPassword() {
        return this.password;
    }

    public String getDateNaissance() {
        return this.dateNaissance;
    }

    public String getTelephone() {
        return this.telephone;
    }

    public String getAdresse() {
        return this.adresse;
    }

    public String getTypeIntervenant() {
        return this.typeIntervenant;
    }

    public String getIdVille() {
        return this.idVille;
    }

    public String getArrondissement() {
        return this.arrondissement;
    }

    public String getHoraire() {
        return this.horaire;
    }

    public void setUserType(String userType) {
        this.userType=userType;
    }

    public void setNom(String nom) {
        this.nom=nom;
    }

    public void setCourriel(String courriel) {
        this.courriel=courriel;
    }

    public void setPassword(String password) {
        this.password=password;
    }

    public void setDateNaissance(String dateNaissance) {
        this.dateNaissance=dateNaissance;
    }

    public void setTelephone(String telephone) {
        this.telephone=telephone;
    }

    public void setAdresse(String adresse) {
        this.adresse=adresse;
    }

    public void setTypeIntervenant(String typeIntervenant) {
        this.typeIntervenant=typeIntervenant;
    }

    public void setIdVille(String idVille) {
        this.idVille=idVille;
    }

    public void setArrondissement(String arrondissement) {
        this.arrondissement=arrondissement;
    }

    public void setHoraire(String horaire) {
        this.horaire=horaire;
    }
}
