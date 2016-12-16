package negociation;
import jade.core.*;
import jade.core.Agent;
import java.util.List;
import java.util.LinkedList;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.util.Logger;

import java.util.Random;
/**
@author Arnaud Cojez, based on Giovanni Caire's Hello World example
*/
public class Neighbour extends Agent {

  static protected Random r;

  private Logger myLogger = Logger.getMyLogger(getClass().getName());
  protected List<Neighbour> neighbours = new LinkedList<Neighbour>();
  protected List<ACLMessage> messagesToSend  = new LinkedList<ACLMessage>();

  protected int oranges;
  protected int tomatoes;
  protected int oranges_desired;
  protected int tomatoes_desired;

  private class WaitPingAndReplyBehaviour extends CyclicBehaviour {

    public WaitPingAndReplyBehaviour(Agent a) {
      super(a);
    }

    //message handling and routing
    public void action() {
      messagesToSend.clear();
      ACLMessage  msg = myAgent.receive();
      if(msg != null){
        if(msg.getPerformative() == ACLMessage.PROPOSE) {
          String content = msg.getContent();
          if ((content != null) && (content.indexOf("orange") != -1)){
            myLogger.log(Logger.INFO, "Agent "+getLocalName()+" - Received ORANGE Proposal from "+msg.getSender().getLocalName());
            handleOrangeProposal(msg);
          }
          if ((content != null) && (content.indexOf("tomato") != -1)){
            myLogger.log(Logger.INFO, "Agent "+getLocalName()+" - Received TOMATO Proposal from "+msg.getSender().getLocalName());
            handleTomatoProposal(msg);
          }
          else{
            myLogger.log(Logger.INFO, "Agent "+getLocalName()+" - Unexpected propose ["+content+"] received from "+msg.getSender().getLocalName());

            ACLMessage reply = msg.createReply();
            reply.setPerformative(ACLMessage.REFUSE);
            reply.setContent("( UnexpectedContent ("+content+"))");
            messagesToSend.add(reply);
          }
        }
        else if(msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
          String content = msg.getContent();
          if ((content != null) && (content.indexOf("orange") != -1)){
            myLogger.log(Logger.INFO, "Agent "+getLocalName()+" - Received ORANGE Accept from "+msg.getSender().getLocalName());
            handleOrangeAccept();
          }
          else if ((content != null) && (content.indexOf("tomato") != -1)){
            myLogger.log(Logger.INFO, "Agent "+getLocalName()+" - Received TOMATO Accept from "+msg.getSender().getLocalName());
            handleTomatoAccept();
          }
          else{
            myLogger.log(Logger.INFO, "Agent "+getLocalName()+" - Unexpected accept ["+content+"] received from "+msg.getSender().getLocalName());

            ACLMessage reply = msg.createReply();
            reply.setPerformative(ACLMessage.REFUSE);
            reply.setContent("( UnexpectedContent ("+content+"))");
            messagesToSend.add(reply);
          }
        }
        else if(msg.getPerformative() == ACLMessage.REJECT_PROPOSAL) {
          String content = msg.getContent();
          if ((content != null) && (content.indexOf("orange") != -1)){
            myLogger.log(Logger.INFO, "Agent "+getLocalName()+" - Received ORANGE Reject from "+msg.getSender().getLocalName());
            handleOrangeRefuse();
          }
          else if ((content != null) && (content.indexOf("tomato") != -1)){
            myLogger.log(Logger.INFO, "Agent "+getLocalName()+" - Received TOMATO Reject from "+msg.getSender().getLocalName());
            handleTomatoRefuse();
          }
          else{
            myLogger.log(Logger.INFO, "Agent "+getLocalName()+" - Unexpected reject ["+content+"] received from "+msg.getSender().getLocalName());

            ACLMessage reply = msg.createReply();
            reply.setPerformative(ACLMessage.REFUSE);
            reply.setContent("( UnexpectedContent ("+content+"))");
            messagesToSend.add(reply);
          }
        }
        else if(msg.getPerformative()== ACLMessage.REQUEST){
          String content = msg.getContent();
          if ((content != null) && (content.indexOf("trade") != -1)){
            myLogger.log(Logger.INFO, "Agent "+getLocalName()+" - Received TRADE Request from "+msg.getSender().getLocalName());
            trade(msg);
          }
          else if ((content != null) && (content.indexOf("list") != -1)) {
            myLogger.log(Logger.INFO, "Agent "+getLocalName()+" - Received LIST Request from "+msg.getSender().getLocalName());
            listNeighbours(msg);
          }
          else if ((content != null) && (content.indexOf("inv") != -1)) {
            myLogger.log(Logger.INFO, "Agent "+getLocalName()+" - Received INVENTORY Request from "+msg.getSender().getLocalName());
            inventory(msg);
          }
          else{
            myLogger.log(Logger.INFO, "Agent "+getLocalName()+" - Unexpected request ["+content+"] received from "+msg.getSender().getLocalName());

            ACLMessage reply = msg.createReply();
            reply.setPerformative(ACLMessage.REFUSE);
            reply.setContent("( UnexpectedContent ("+content+"))");
            messagesToSend.add(reply);
          }

        }
        else {
          myLogger.log(Logger.INFO, "Agent "+getLocalName()+" - Unexpected message ["+ACLMessage.getPerformative(msg.getPerformative())+"] received from "+msg.getSender().getLocalName());

          ACLMessage reply = msg.createReply();
          reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
          reply.setContent("( (Unexpected-act "+ACLMessage.getPerformative(msg.getPerformative())+") )");
          messagesToSend.add(reply);
        }
        for(ACLMessage reply : messagesToSend) {
          send(reply);
          System.out.println(myAgent.getLocalName() + " will send " + reply);
        }
      }
      else {
        block();
      }
    }

    //REQUESTS===========================

    protected void listNeighbours(ACLMessage msg) {
      ACLMessage reply = msg.createReply();
      String res = "";
      reply.setPerformative(ACLMessage.INFORM);

      for (Neighbour n : neighbours) {
        res += n.toString() + " ";
      }

      reply.setContent(res);
      messagesToSend.add(reply);
    }

    protected void inventory(ACLMessage msg) {
      ACLMessage reply = msg.createReply();
      String res = "";
      reply.setPerformative(ACLMessage.INFORM);

      reply.setContent(myAgent.getLocalName() + " : Tomatoes = " + tomatoes + "/" +  tomatoes_desired + ", Oranges = " + oranges + "/" + oranges_desired);
      messagesToSend.add(reply);
    }

    protected void trade(ACLMessage msg) {
      String res = "";

      if(oranges > oranges_desired) {
        res += "orange";
      }
      if(tomatoes > tomatoes_desired) {
        res += "tomato";
      }
      if(res == "")
        return ;

      ACLMessage reply = new ACLMessage(ACLMessage.PROPOSE);

      for (Neighbour n : neighbours) {
        reply.addReceiver(new AID(n.getLocalName(), AID.ISLOCALNAME));
      }

      reply.addReceiver(neighbours.get(0).getAID());
      reply.setContent(res);
      messagesToSend.add(reply);
    }

    //TRADE=============================

    protected void handleOrangeProposal(ACLMessage msg) {
      if(oranges < oranges_desired) {
        acceptOrange(msg);
      } else {
        refuseOrange(msg);
      }
    }

    protected void acceptOrange(ACLMessage msg) {
      ACLMessage reply = msg.createReply();
      reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
      reply.setContent("orange");
      oranges += 1;
      //send message
      messagesToSend.add(reply);
    }

    protected void refuseOrange(ACLMessage msg) {
      ACLMessage reply = msg.createReply();
      reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
      reply.setContent("orange");
      //send message
      messagesToSend.add(reply);
    }


    protected void handleTomatoProposal(ACLMessage msg) {
      if(tomatoes < tomatoes_desired) {
        acceptTomato(msg);
      } else {
        refuseTomato(msg);
      }
    }

    protected void acceptTomato(ACLMessage msg) {
      ACLMessage reply = msg.createReply();
      reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
      reply.setContent("tomato");
      tomatoes += 1;
      //send message
      messagesToSend.add(reply);
    }

    protected void refuseTomato(ACLMessage msg) {
      ACLMessage reply = msg.createReply();
      reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
      reply.setContent("tomato");
      //send message
      messagesToSend.add(reply);
    }



    protected void handleOrangeAccept() {
      if(oranges < 1) {
        throw new IllegalStateException("oranges < 1 when accepting trade");
      } else {
        oranges -= 1;
      }
    }

    protected void handleTomatoAccept() {
      if(tomatoes < 1) {
        throw new IllegalStateException("tomatoes < 1 when accepting trade");
      } else {
        tomatoes -= 1;
      }
    }

    protected void handleOrangeRefuse() {

    }

    protected void handleTomatoRefuse() {

    }

  }

  //SETUP============================

  protected void setup() {
    r = new Random();
    oranges_desired = r.nextInt(10);
    tomatoes_desired = r.nextInt(10);
    oranges = 10;
    tomatoes = 10;
    System.out.println("Hello World! My name is "+getLocalName() + " " + oranges + "/" + oranges_desired + " " + tomatoes + "/" + tomatoes_desired);

    // Registration with the DF
    DFAgentDescription dfd = new DFAgentDescription();
    ServiceDescription sd = new ServiceDescription();
    sd.setType("Neighbour");
    sd.setName(getName());
    sd.setOwnership("Cojez");
    dfd.setName(getAID());
    dfd.addServices(sd);
    try {
      DFService.register(this,dfd);
      WaitPingAndReplyBehaviour PingBehaviour = new  WaitPingAndReplyBehaviour(this);
      addBehaviour(PingBehaviour);
    } catch (FIPAException e) {
      myLogger.log(Logger.SEVERE, "Agent "+getLocalName()+" - Cannot register with DF", e);
      doDelete();
    }
  }


  public String toString() {
    return getLocalName();
  }

  protected void addNeighbour(Neighbour n) {
    this.neighbours.add(n);
  }

  protected void removeNeighbour(Neighbour n) {
    this.neighbours.remove(n);
  }

  protected boolean hasNeighbour(Neighbour n) {
    return this.neighbours.contains(n);
  }

  public List<Neighbour> getNeighbours() {
    return neighbours;
  }
}
