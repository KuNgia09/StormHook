
typedef struct{
	const char *tClazz;
	const char *tMethod;
	const char *tMeihodSig;
	void *handleFunc;
} HookInfo;

typedef int(*SetupFunc)(HookInfo**);

int Hook();
