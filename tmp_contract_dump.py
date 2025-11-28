from dump_project import create_dump_file, PROJECT_ROOT

FILES = [
    # API and data layer
    "opensource/src/main/java/ua/gov/diia/opensource/data/contracts/api/ContractsApi.kt",
    "opensource/src/main/java/ua/gov/diia/opensource/data/contracts/di/ContractsApiModule.kt",
    "opensource/src/main/java/ua/gov/diia/opensource/data/contracts/repo/ContractsRepository.kt",
    "opensource/src/main/java/ua/gov/diia/opensource/data/contracts/storage/ClientIdStorage.kt",
    # ViewModels and business logic
    "opensource/src/main/java/ua/gov/diia/opensource/ui/compose/ContractsFlowViewModel.kt",
    "opensource/src/main/java/ua/gov/diia/opensource/ui/compose/ContractsMenuViewModel.kt",
    "opensource/src/main/java/ua/gov/diia/opensource/ui/compose/ContractsVM.kt",
    # UI flow for contract creation
    "opensource/src/main/java/ua/gov/diia/opensource/ui/compose/CreateContractFlow.kt",
    "opensource/src/main/java/ua/gov/diia/opensource/ui/compose/CreateContractFCompose.kt",
    "opensource/src/main/java/ua/gov/diia/opensource/ui/compose/ContractCreationStep.kt",
    "opensource/src/main/java/ua/gov/diia/opensource/ui/compose/ContractsFCompose.kt",
    "opensource/src/main/java/ua/gov/diia/opensource/ui/compose/ContractsScreen.kt",
    "opensource/src/main/java/ua/gov/diia/opensource/ui/compose/ContractDetailsScreen.kt",
    "opensource/src/main/java/ua/gov/diia/opensource/ui/compose/ContractPreviewScreen.kt",
    "opensource/src/main/java/ua/gov/diia/opensource/ui/compose/LeaseContractFormComposeF.kt",
    "opensource/src/main/java/ua/gov/diia/opensource/ui/compose/LeaseContractFormComposeVM.kt",
    # Navigation and deep links
    "opensource/src/main/java/ua/gov/diia/opensource/deeplinkprocessor/DeepLinkActionJoinContractProcessor.kt",
    "opensource/src/main/res/navigation/nav_contracts.xml",
    "opensource/src/main/res/navigation/nav_lease_contract.xml",
]

parts: list[str] = []
for rel in FILES:
    path = PROJECT_ROOT / rel
    parts.append(f"===== FILE: {rel} =====")
    try:
        parts.append(path.read_text(encoding="utf-8"))
    except FileNotFoundError:
        parts.append("<<missing>>")
    parts.append("")

content = "\n".join(parts)
file_path = create_dump_file(content, file_name="contract_flow_scripts")
print(file_path)
