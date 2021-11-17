package socialNetwork.repository.csv;

import socialNetwork.domain.models.Entity;
import socialNetwork.exceptions.IOFileException;
import socialNetwork.repository.memory.InMemoryRepository;

import java.io.*;
import java.util.List;
import java.util.Optional;

public abstract class AbstractCsvFileRepository<ID, E extends Entity<ID>> extends InMemoryRepository<ID, E> {
    private final String filePath;

    /**
     * constructor - creates a new repo that accesses the file found at filePath
     * @param filePath - String - absolute path to file
     * throw {@link IOFileException} - file cannot be opened
     */
    public AbstractCsvFileRepository(String filePath) {
        this.filePath = filePath;
        loadDataFromFile();
    }

    /**
     * converts a String of model into a model object
     * @param entityAsString - String
     * @return - Entity
     * throw {@link socialNetwork.exceptions.CorruptedDataException} - given string does not respect csv format
     */
    public abstract E stringToEntity(String entityAsString);

    /**
     * converts an Entity object into a String of values
     * @param entity - Entity object
     * @return - String
     */
    public abstract String entityToString(E entity);

    @Override
    public Optional<E> save(E entityToSave) {
        loadDataFromFile();
        Optional<E> entityOptionalReturn = super.save(entityToSave);
        if (entityOptionalReturn.isEmpty())
            appendEntityToFile(entityToSave);
        return entityOptionalReturn;
    }

    @Override
    public Optional<E> remove(ID idEntity) {
        loadDataFromFile();
        Optional<E> entityOptionalReturn = super.remove(idEntity);
        if(entityOptionalReturn.isPresent())
            storeToFile();
        return entityOptionalReturn;
    }

    @Override
    public Optional<E> update(E updatedEntity) {
        loadDataFromFile();
        Optional<E> optionalEntityReturn = super.update(updatedEntity);
        if(optionalEntityReturn.isPresent())
            storeToFile();
        return optionalEntityReturn;
    }

    @Override
    public Optional<E> find(ID idSearchedEntity) {
        loadDataFromFile();
        return super.find(idSearchedEntity);
    }

    @Override
    public List<E> getAll() {
        loadDataFromFile();
        return super.getAll();
    }

    /**
     * loads all local data from a csv file
     * throw {@link IOFileException} - file cannot be opened
     */
    private void loadDataFromFile() {
        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath))){
            super.removeAllLocalData();
            String currentLine;
            while((currentLine = bufferedReader.readLine()) != null){
                currentLine = currentLine.stripLeading().stripTrailing();
                if(currentLine.length() > 0) {
                    E currentEntity = stringToEntity(currentLine);
                    super.save(currentEntity);
                }
            }
        } catch (IOException e) {
            throw new IOFileException("Cannot load data from " + filePath + "\n");
        }
    }

    /**
     * stores all local data into a csv file
     * throw {@link IOFileException} - file cannot be opened
     */
    private void storeToFile(){
        try(BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filePath))) {
            for(E entity: super.getAll()){
                bufferedWriter.write(entityToString(entity));
                bufferedWriter.newLine();
            }
        } catch (IOException e){
            throw new IOFileException("Cannot load data from file " + filePath + "\n");
        }
    }

    /**
     * appends a new entity in string format into a csv file
     * @param entity - Entity subclass instance
     * throw {@link IOFileException} - file cannot be opened
     */
    private void appendEntityToFile(E entity){
        try(BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filePath, true))) {
            bufferedWriter.write(entityToString(entity));
            bufferedWriter.newLine();
        } catch (IOException e){
            throw new IOFileException("Cannot load data from file " + filePath + "\n");
        }
    }
}
