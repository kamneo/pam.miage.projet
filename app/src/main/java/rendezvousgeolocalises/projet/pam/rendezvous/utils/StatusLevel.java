package rendezvousgeolocalises.projet.pam.rendezvous.utils;


public interface StatusLevel {
    int WAITING_FOR_VALIDATION = 0;
    int ACCEPTED = 1;
    int REFUSED = 2;
    int CANCELED = 3;
    int CREATOR = 4;
}
