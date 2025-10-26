package models

type Complaint struct {
	ID          string `json:"id"`
	User        string `json:"user"`
	Message     string `json:"message"`
	Status      string `json:"status"`
	Category    string `json:"category"`
	Created     string `json:"created"`
}
