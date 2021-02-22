import java.util.*;
public class GeneticAlgorithm {

    final static int generations = 30;
    final static int populationSize = 100;
    static int geneLength = -1;
    final static double mutationRate = 0.1;
    
    //0 : boolean
    //1 : int
    //2 : string
    final static int type = 2;
    
    //0 : until reached target
    //1 : until reached deadline
    //2 : until maximum generations
    //3 : no halt
    final static int mode = 3;
    static Object[] goal = null;
    
    static LinkedList<Creature> population = null;

    public static void main(String[] args) {
        
        //put your sentence here
        String sentence = "Target!";

        goal = new Object[sentence.length()];
        for(int i=0; i<sentence.length(); i++) {
            goal[i] = sentence.charAt(i);
        }

        geneLength = goal.length;

        checkGoalConformity();

        createPopulation();
        train();
    }
    private static void checkGoalConformity() {
        if(goal == null) {
            System.out.println("Define goal");
            System.exit(-1);
        }else if(goal.length != geneLength) {
            System.out.println("Gene must be same size as goal. "+goal.length+" vs. "+geneLength);
            System.exit(-1);
        }
    }
    public static void createPopulation() {
        population = new LinkedList<Creature>();
        for(int i=0; i<populationSize; i++) {
            Creature creature = new Creature();
            creature.createNewGene();
            //creature.printCreatureGene();
            population.add(creature);
        }
    }
    public static void train() {
        //Minimize Score --> Score is measurement for distance
        Game game = new Game(population);
        if(mode == 0) {
            //TODO: Until first score==0
        } else if(mode == 1) {
            //TODO: create extra watchdog thread
        } else if(mode == 2) {
            for(int i=0; i<generations; i++) {
                game.selection();
                game.creation();
            }
        } else if(mode == 3) {
            int i=0;
            while(true) {
                //never ending story <3
                System.out.println("Generation: "+i);
                i++;
                game.selection();
                game.creation();
                try {
                    Thread.currentThread().sleep(100);
                } catch (Exception e) {
                    
                }
            }
        } else {}
    }
    public static class Game {
        LinkedList<Creature> population = null;
        public Game(LinkedList<Creature> population) {
            this.population = population;
        }
        public int getScore(Creature creature) {
            int score = 0;
            for(int i=0; i<creature.gene.length; i++) {
                score += (creature.gene[i]==goal[i]) ? 0 : 1;
            }
            return score;
        }
        public void creation() {
            int need = populationSize-population.size();
            int newSize = need;
            int mutationSize = 0;
            int crossoverSize = 0;
            
            if(population.size()>0) {
                newSize = need/3;
                mutationSize = need/3;
                crossoverSize = need-newSize-mutationSize;
            }
            
            LinkedList<Creature> tmp = new LinkedList<Creature>();
            
            //new creatures
            for(int i=0; i<newSize; i++) {
                Creature creature = new Creature();
                creature.createNewGene();
                tmp.addLast(creature);
            }
            //mutation
            for(int i=0; i<mutationSize; i++) {
                Creature creature = new Creature();
                
                //choose an existing creature
                int index = (int)Math.floor(Math.random()*(population.size()));
                creature.gene = population.get(index).copyGene();
                creature.mutate();
                
                tmp.addLast(creature);
            }
            //crossover
            for(int i=0; i<crossoverSize; i++) {
                //choose an existing creature
                double d = Math.random();
                
                int index = (int)Math.floor(d*(population.size()));
                d = Math.random();
                int index2 = (int)Math.floor(d*(population.size()-1));
                if(index2>=index) {
                    index2++;
                }
                if(population.size()==1) {
                    index2 = index;
                }
                Creature creature1 = population.get(index);
                Creature creature2 = population.get(index2);
                Creature creature = creature1.crossover(creature1,creature2);
                tmp.addLast(creature);
            }
            population.addAll(tmp);
        }
        public void selection() {
            LinkedList<Integer> ranking = new LinkedList<Integer>();
            int[] scores = new int[population.size()];
            
            //with this helper, we go at least 1 time into the second for-loop
            ranking.add(Integer.MAX_VALUE);
            
            for(int i=0; i<population.size(); i++) {
                int score = getScore(population.get(i));
                for(int j=0; j<ranking.size(); j++) {
                    if(ranking.get(j)==Integer.MAX_VALUE) {
                        ranking.add(j,i);
                        scores[i] = score;
                        break;
                    } else if(scores[ranking.get(j)]>score) {
                        ranking.add(j,i);
                        scores[i] = score;
                        break;
                    }
                }
            }
            //remove MAX_VALUE helper
            ranking.removeLast();
            
            //Ranking
            System.out.println("---------------------------------------------------");
            for(int i=0; i<3; i++) {
                int index = ranking.get(i);
                System.out.print(index+"\t"+scores[index]+"\t");
                population.get(index).printCreatureGene();
            }
            
            //exponential function
            double participants = population.size();
            LinkedList<Creature> survivor = new LinkedList<Creature>();
            for(int i=0; i<participants; i++) {
                double chance = 1*Math.pow(Math.E,-0.618*Math.PI*((i+1)/participants));
                if(Math.random()<chance) {
                    //System.out.println("Kept: "+scores[ranking.get(i)]);
                    survivor.add(population.get(ranking.get(i)));
                } else {
                    //System.out.println("Removed: "+scores[ranking.get(i)]);
                }
            }
            population = new LinkedList<Creature>(survivor);
        }
    }
    public static class Creature {
        Object[] gene = null;
        public Creature() {
            gene = new Object[geneLength];
        }
        public void createNewGene() {
            for(int i=0; i<gene.length; i++) {
                gene[i] = generateInformation();
            }
        }
        private Object generateInformation() {
            double random = Math.random();
            if(type == 0) {
                random*=2;
                return (int)Math.floor(random);
            } else if(type == 1) {
                random*=10;
                return (int)Math.floor(random);
            } else if(type == 2) {
                //Full ASCII range (32 to 126)
                random*=94;
                random+=32;
                /*
                //26 big and small letters and space
                random*=53;
                if(random<1) {
                    random+=32;
                } else if(random<27) {
                    //A is 65 and random must be at least 1
                    random+=64;
                } else {
                    //a is 97 and random must be at least 27
                    random+=70;
                }
                */
                return (char)Math.floor(random);
            }
            return null;
        }
        public void printCreatureGene() {
            System.out.print("[");
            for(int i=0; i<gene.length; i++) {
                System.out.print(gene[i]);
                if(i<gene.length-1) {
                    System.out.print(" ");
                }
            }
            System.out.println("]");
        }
        public Object[] copyGene() {
            Object[] otherGene = new Object[gene.length];
            for(int i=0; i<otherGene.length; i++) {
                otherGene[i] = gene[i];
            }
            return otherGene;
        }
        public void mutate() {
            for(int i=0; i<gene.length; i++) {
                if(Math.random() < mutationRate) {
                    gene[i] = generateInformation();
                }
            }
        }
        public static Creature crossover(Creature creature1, Creature creature2) {
            Creature crossoverCreature = new Creature();
            crossoverCreature.gene = creature1.copyGene();
            for(int i=0; i<crossoverCreature.gene.length; i++) {
                if(Math.random() < 0.5) {
                    crossoverCreature.gene[i] = creature2.gene[i];
                }
            }
            return crossoverCreature;
        }
    }
}
