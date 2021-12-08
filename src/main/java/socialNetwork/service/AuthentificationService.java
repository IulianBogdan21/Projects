package socialNetwork.service;

import socialNetwork.domain.models.Autentification;
import socialNetwork.domain.validators.AuthentificationValidator;
import socialNetwork.domain.validators.EntityValidatorInterface;
import socialNetwork.repository.RepositoryInterface;

import java.net.Authenticator;
import java.util.List;
import java.util.Optional;

public class AuthentificationService {

    RepositoryInterface<String, Autentification> repoAuthentification;
    EntityValidatorInterface<String,Autentification> authentificationValidator;

    public AuthentificationService(RepositoryInterface<String, Autentification> repoAuthentification, EntityValidatorInterface<String,Autentification> authentificationValidator) {
        this.repoAuthentification = repoAuthentification;
        this.authentificationValidator = authentificationValidator;
    }

    public Optional<Autentification> saveAuthentificationService(String username,String password){
        Autentification autentification = new Autentification(username, password);
        authentificationValidator.validate(autentification);
        Optional<Autentification> saveAutentification = repoAuthentification.save(autentification);
        return saveAutentification;
    }

    public Optional<Autentification> findAuthentificationService(String username){
        Optional<Autentification> findAuthentification = repoAuthentification.find(username);
        return findAuthentification;
    }

    public List<Autentification> getAllAuthentificationService(){
        return repoAuthentification.getAll();
    }
}
