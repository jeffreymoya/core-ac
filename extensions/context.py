from copier_templates_extensions import ContextHook


class ContextUpdater(ContextHook):
    def hook(self, context):
        new_context = {}
        new_context["artifact_id"] = context["artifact_id_raw"].replace('-', '')
        return new_context
