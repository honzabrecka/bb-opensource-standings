FROM clojure:boot-2.7.2

RUN curl -sL https://deb.nodesource.com/setup_8.x | bash - \
  && apt-get install -y nodejs \
  && apt-get --purge autoremove -y \
  && npm install -g lumo-cljs --unsafe-perm

ENV TOKEN_GIHUB ""

WORKDIR /usr/src/app

COPY package.json package-lock.json deps.edn ./

RUN npm install && boot -d funcool/promesa:1.9.0

COPY . .

ENTRYPOINT ["/usr/bin/lumo", "-c", "src", "-D", "funcool/promesa:1.9.0"]
CMD ["-e", "(require 'sandbox.github) (sandbox.github/standings)"]
