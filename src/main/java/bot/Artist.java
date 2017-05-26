package bot;

public class Artist {

	private String nome;
	private String location;
	private String arte;
	private String link;
	
	Artist(String n, String l, String a, String lin){
		nome = n;
		location = l;
		arte = a;
		link = lin;
	}


	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getNome() {
		return nome;
	}
	public String getArte() {
		return arte;
	}

	public void setArte(String arte) {
		this.arte = arte;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String mountArtist(){
		return this.getNome() + "\nLocation: " + this.getLocation();
	}
}
