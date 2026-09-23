# Pass 23 walk. Names one component the SignatureFinance read already holds.
resource "azurerm_kubernetes_cluster" "pass23_walk" {
  name                = "aks-harnessmatters-eus2"
  resource_group_name = "rg-harnessmatters"
}
