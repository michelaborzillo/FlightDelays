package it.polito.tdp.extflightdelays.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

import it.polito.tdp.extflightdelays.db.ExtFlightDelaysDAO;

public class Model {

	private Graph<Airport, DefaultWeightedEdge> grafo;
	private ExtFlightDelaysDAO dao;
	private Map<Integer, Airport> idMap ;
	
	public Model () {
		dao= new ExtFlightDelaysDAO();
		idMap= new HashMap<Integer, Airport>();
		dao.loadAllAirports(idMap);
	}
	
	public void creaGrafo (int x) {
		grafo = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		
		//aggiungo i vertici
		Graphs.addAllVertices(this.grafo, dao.getVertici(x, idMap));
		
		//aggiungo gli archi
		for (Rotta r: dao.getRotte(idMap)) {
			if (this.grafo.containsVertex(r.getA1())&& this.grafo.containsVertex(r.getA2())) {
			DefaultWeightedEdge edge= this.grafo.getEdge(r.getA1(), r.getA2()); //arco non orientato, non mi importa qual è la sorgente o la destinazione
			if (edge==null) //se aeroporti ancora non collegati
				Graphs.addEdgeWithVertices(this.grafo, r.getA1(), r.getA2(), r.getnVoli()); 
			else {
				double pesoVecchio= this.grafo.getEdgeWeight(edge); //restituisce il peso attuale
				double pesoNuovo= pesoVecchio+r.getnVoli();
				this.grafo.setEdgeWeight(edge, pesoNuovo);
			}
			}
			
		}
		
		System.out.println("VERTICI: "+this.grafo.vertexSet().size()+"\n");
		System.out.println("ARCHI: "+this.grafo.edgeSet().size()+"\n");
	}
	
	
	public List<Airport> getVertici () {
		
		List<Airport> vertici = new ArrayList<Airport>(this.grafo.vertexSet()); //uso la set per riempire la lista
		Collections.sort(vertici);
		return vertici;
		
		
	}
	
	public List<Airport> getPercorso (Airport a1, Airport a2) { //PER STAMPARE IL PERCORSO, PUNTO d
		List<Airport> percorso = new ArrayList<Airport>();
		
		//passo vertici e archi
		BreadthFirstIterator<Airport, DefaultWeightedEdge> it= new BreadthFirstIterator<>(this.grafo, a1); //indico grafo e nodo di partenza da cui voglio partire
		boolean trovato = false;
		
		
		//visito il grafo
		while (it.hasNext()) {
			Airport visitato= it.next(); //ritorna il nodo che visita
			if (visitato.equals(a2))
				trovato = true;
			
			
		}
		//ottengo il percorso
		if (trovato) {
		percorso.add(a2); //aggiungo la destinazione
		Airport step = it.getParent(a2); //risalgo dalla destinazione, al padre
		//Vado avanti prendendo il padre, fino a quando non arrivo alla sorgente
		while (!step.equals(a1)) {
			percorso.add(0,step); //aggiungo in testa
			step=it.getParent(step);
			
	
		}
		
		percorso.add(0,a1);
		return percorso;
		}
		else {
			return null;
		}
	}
	
	
}
