package com.sma;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/***
 * An Agent for the system
 * An Agent is a Thread : all agents are running at the same time
 */
public class Agent extends Thread {

    public Agent(int tailleMem) {
        memoire = new LinkedList<>();
        this.tailleMem = tailleMem;
    }

    // CARACTERISTIQUES

    private Objet porte;
    private LinkedList<String> memoire;
    private int tailleMem;

    private void addMemoire(String type) {
        if (memoire.size() >= tailleMem) {
            memoire.push(type);
            memoire.poll();
        } else {
            memoire.push(type);
        }
    }

    private void addMemoireMouvement(List<String> types) {
        types.forEach(s -> addMemoire(s));
    }

    // ACTIONS

    private void seDeplacer() {
        Board env = Board.getInstance();
        Random random = new Random();

        int dir = random.nextInt(4);
        String direction;

        switch (dir) {
            case 0: direction = "N";
                break;
            case 1: direction = "S";
                break;
            case 2: direction = "E";
                break;
            default: direction = "O";
                break;
        }

        addMemoireMouvement(env.seDeplacer(direction, this));
    }

    private void prendre() {
        Board env = Board.getInstance();

        porte = env.prendre(this);
    }

    private void deposer() {
        Board env = Board.getInstance();

        porte = env.deposer(this, porte);
    }

    // PERCEPTIONS

    private Case caseActuelle() {
        Board env = Board.getInstance();

        return env.getCase(this);
    }

    private List<Case> casesVoisines() {
        Board env = Board.getInstance();

        return env.getVoisins(this);
    }

    // Probabilités

    private boolean doitPrendre(String type, List<Case> voisins) {
        Board env = Board.getInstance();
        Random random = new Random();

        float f = fQuestion2(type, voisins);

        float proba = (env.getkPlus() / (env.getkPlus() + f)) * (env.getkPlus() / (env.getkPlus() + f));
        float randValue = random.nextFloat();

        return randValue < proba;
    }

    private boolean doitDeposer(String type, List<Case> voisins) {
        Board env = Board.getInstance();
        Random random = new Random();

        float f = fQuestion1(type, voisins);

        float proba = (f / (env.getkMoins() + f)) * (f / (env.getkMoins() + f));
        float randValue = random.nextFloat();

        return randValue < proba;
    }

    private float fQuestion1(String type, List<Case> voisins) {
        float f = 0;
        for (Case c : voisins) {
            if (c.getObjet() != null && c.getObjet().getType() == type)
                ++f;
        }
        f /= voisins.size();

        return f;
    }

    private float fQuestion2(String type, List<Case> voisins) {
        Board env = Board.getInstance();
        Random random = new Random();

        String autreType;
        if (type.equals("A")) {
            autreType = "B";
        } else {
            autreType = "A";
        }

        return (nbTypeEnMemoire(type) + nbTypeEnMemoire(autreType) * env.getErreur()) / memoire.size();
    }

    private String reconnaissanceObjet(String typeInitial) {
        Board env = Board.getInstance();
        Random random = new Random();

        String autreType;
        if (typeInitial.equals("A")) {
            autreType = "B";
        } else {
            autreType = "A";
        }

        float proba = (nbTypeEnMemoire(typeInitial) + nbTypeEnMemoire(autreType) * env.getErreur()) / memoire.size();

        float randValue = random.nextFloat();
        if (randValue < proba) {
            return autreType;
        } else {
            return typeInitial;
        }
    }

    private int nbTypeEnMemoire(String type) {
        int counter = 0;
        for (String s: memoire) {
            if (s.equals(type))
                ++counter;
        }

        return counter;
    }

    // COMPORTEMENT

    @Override
    public void run() {

        Board env = Board.getInstance();
        int iter = 0;

        while (iter < env.getNbIter()) {
            // Perceptions :

            Case caseActuelle = caseActuelle();
            List<Case> voisins = casesVoisines();

            // Actions

            if (porte == null) {
                if (caseActuelle.getObjet() != null && doitPrendre(caseActuelle.getObjet().getType(), voisins)) {
                    prendre();
                }
            } else {
                if (caseActuelle.getObjet() == null && doitDeposer(porte.getType(), voisins)) {
                    deposer();
                }
            }

            seDeplacer();

            ++iter;
        }
    }
}
